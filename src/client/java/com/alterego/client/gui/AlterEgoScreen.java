package com.alterego.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.alterego.AlterEgo;
import com.alterego.EgoSelection;
import com.alterego.ability.Ability;
import com.alterego.ability.AbilityRegistry;
import com.alterego.client.ClientEgoState;
import com.alterego.net.SelectEgoPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;

import org.jetbrains.annotations.Nullable;

/**
 * Fullscreen ego picker: live preview of the selected entity up top, a scrollable
 * grid of all living entity types below, options (nametag, abilities) in the
 * top-left corner, Apply/Revert at the bottom.
 */
public class AlterEgoScreen extends Screen {
	private static final int MARGIN = 20;
	private static final int CELL_W = 40;
	private static final int CELL_H = 48;
	private static final int PREVIEW_W = 110;
	private static final int PREVIEW_TOP = 26;
	private static final int PREVIEW_H = 112;
	private static final int SEARCH_W = 220;
	private static final int SEARCH_H = 18;

	/** Preview instances by type; entries are created once per screen and reused for grid cells. */
	private final Map<EntityType<?>, LivingEntity> previewCache = new HashMap<>();
	private final List<EntityType<?>> allTypes = new ArrayList<>();
	private final List<EntityType<?>> filteredTypes = new ArrayList<>();

	/** On by default now that supported mods have ability mappings; remembered for the session. */
	private static boolean showCustomEntities = true;

	@Nullable
	private EntityType<?> selectedType;
	private boolean showNametag;
	private final Set<String> enabledAbilities = new HashSet<>();
	private String search = "";
	private double scrollY;

	private final List<Checkbox> abilityCheckboxes = new ArrayList<>();
	private EditBox searchBox;

	private int gridX0;
	private int gridY0;
	private int gridY1;
	private int columns;

	public AlterEgoScreen() {
		super(Component.translatable("screen.alterego.title"));
		LocalPlayer player = Minecraft.getInstance().player;
		EgoSelection current = player == null ? EgoSelection.SELF : ClientEgoState.get(player.getUUID());
		this.selectedType = current.entityType();
		this.showNametag = current.showNametag();
		this.enabledAbilities.addAll(current.enabledAbilities());
	}

	@Override
	protected void init() {
		if (this.allTypes.isEmpty()) {
			collectLivingTypes();
		}

		// Preview box, then the entity name line under it, then the search
		// field, then the grid — each gets its own breathing room.
		int searchY = PREVIEW_TOP + PREVIEW_H + 4 + this.font.lineHeight + 10;
		this.gridY0 = searchY + SEARCH_H + 10;
		this.gridY1 = this.height - 40;
		this.columns = Math.max(1, (this.width - 2 * MARGIN) / CELL_W);
		this.gridX0 = (this.width - this.columns * CELL_W) / 2;

		this.searchBox = new EditBox(this.font, (this.width - SEARCH_W) / 2, searchY,
				SEARCH_W, SEARCH_H, this.searchBox, Component.translatable("screen.alterego.search"));
		this.searchBox.setHint(Component.translatable("screen.alterego.search").withStyle(s -> EditBox.SEARCH_HINT_STYLE));
		this.searchBox.setResponder(text -> {
			this.search = text;
			refilter();
		});
		this.addRenderableWidget(this.searchBox);

		this.addRenderableWidget(Checkbox.builder(Component.translatable("screen.alterego.show_nametag"), this.font)
				.pos(MARGIN, PREVIEW_TOP + 4)
				.selected(this.showNametag)
				.onValueChange((box, value) -> this.showNametag = value)
				.build());

		Checkbox customBox = Checkbox.builder(Component.translatable("screen.alterego.show_custom"), this.font)
				.pos(0, PREVIEW_TOP + 4)
				.selected(showCustomEntities)
				.onValueChange((box, value) -> {
					showCustomEntities = value;
					refilter();
				})
				.build();
		customBox.setX(this.width - MARGIN - customBox.getWidth());
		this.addRenderableWidget(customBox);

		this.addRenderableWidget(Button.builder(Component.translatable("screen.alterego.apply"), b -> apply())
				.bounds(this.width / 2 - 105, this.height - 30, 100, 20).build());
		this.addRenderableWidget(Button.builder(Component.translatable("screen.alterego.revert"), b -> revert())
				.bounds(this.width / 2 + 5, this.height - 30, 100, 20).build());

		rebuildAbilityCheckboxes();
		refilter();
	}

	private void collectLivingTypes() {
		// Preview entities are never added to the level, so no ID is assigned to
		// them; getId() throws in that state and some renderers call it. Hand out
		// unique negative IDs, which real (positive-ID) entities never use.
		int previewId = -1;
		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
			if (type == EntityTypes.PLAYER) {
				continue;
			}
			try {
				if (type.create(this.minecraft.level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true)) instanceof LivingEntity living) {
					living.setId(previewId--);
					this.previewCache.put(type, living);
					this.allTypes.add(type);
				}
			} catch (Exception e) {
				AlterEgo.LOGGER.debug("Skipping entity type {} (failed to create preview)", EntityType.getKey(type), e);
			}
		}
		this.allTypes.sort((a, b) -> a.getDescription().getString().compareToIgnoreCase(b.getDescription().getString()));
	}

	private void refilter() {
		this.filteredTypes.clear();
		String query = this.search.trim().toLowerCase(Locale.ROOT);
		for (EntityType<?> type : this.allTypes) {
			if (!showCustomEntities && !Identifier.DEFAULT_NAMESPACE.equals(EntityType.getKey(type).getNamespace())) {
				continue;
			}
			if (query.isEmpty() || type.getDescription().getString().toLowerCase(Locale.ROOT).contains(query)) {
				this.filteredTypes.add(type);
			}
		}
		this.scrollY = Mth.clamp(this.scrollY, 0.0, maxScroll());
	}

	private void rebuildAbilityCheckboxes() {
		for (Checkbox box : this.abilityCheckboxes) {
			removeWidget(box);
		}
		this.abilityCheckboxes.clear();

		List<Ability> abilities = this.selectedType == null ? List.of() : AbilityRegistry.abilitiesFor(this.selectedType);
		int y = PREVIEW_TOP + 40;
		for (Ability ability : abilities) {
			Checkbox box = Checkbox.builder(Component.translatable(ability.translationKey()), this.font)
					.pos(MARGIN, y)
					.selected(this.enabledAbilities.contains(ability.id()))
					.onValueChange((cb, value) -> {
						if (value) {
							this.enabledAbilities.add(ability.id());
						} else {
							this.enabledAbilities.remove(ability.id());
						}
					})
					.build();
			this.abilityCheckboxes.add(box);
			this.addRenderableWidget(box);
			y += Checkbox.getBoxSize(this.font) + 4;
		}
	}

	private void selectType(@Nullable EntityType<?> type) {
		this.selectedType = type;
		this.enabledAbilities.clear();
		if (type != null) {
			// Abilities default to enabled on a fresh selection.
			for (Ability ability : AbilityRegistry.abilitiesFor(type)) {
				this.enabledAbilities.add(ability.id());
			}
		}
		rebuildAbilityCheckboxes();
	}

	private void apply() {
		if (this.minecraft.player != null) {
			EgoSelection selection = new EgoSelection(this.selectedType, this.showNametag, Set.copyOf(this.enabledAbilities));
			// Optimistic local update; the server's sync broadcast is authoritative.
			ClientEgoState.set(this.minecraft.player.getUUID(), selection);
			ClientPlayNetworking.send(SelectEgoPayload.of(selection));

			Component message = this.selectedType == null
					? Component.translatable("screen.alterego.reverted")
					: Component.translatable("screen.alterego.applied", this.selectedType.getDescription());
			this.minecraft.gui.hud.setOverlayMessage(message, false);
		}
		onClose();
	}

	private void revert() {
		selectType(null);
		apply();
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.blurBeforeThisStratum();
		graphics.fillGradient(0, 0, this.width, this.height, 0xB0101018, 0xC8101018);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		graphics.centeredText(this.font, this.title, this.width / 2, 10, 0xFFFFFFFF);

		extractPreview(graphics, mouseX, mouseY);
		if (!this.abilityCheckboxes.isEmpty()) {
			graphics.text(this.font, Component.translatable("screen.alterego.abilities"), MARGIN, PREVIEW_TOP + 28, 0xFFA0A0A0);
		}
		extractGrid(graphics, mouseX, mouseY);

		super.extractRenderState(graphics, mouseX, mouseY, a);
	}

	private void extractPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		int x0 = (this.width - PREVIEW_W) / 2;
		int x1 = x0 + PREVIEW_W;
		int y1 = PREVIEW_TOP + PREVIEW_H;
		graphics.fill(x0, PREVIEW_TOP, x1, y1, 0x60000000);
		graphics.outline(x0, PREVIEW_TOP, PREVIEW_W, PREVIEW_H, 0xFF404048);

		LivingEntity entity = this.selectedType == null ? this.minecraft.player : this.previewCache.get(this.selectedType);
		if (entity != null) {
			int size = previewSize(entity, PREVIEW_H);
			InventoryScreen.extractEntityInInventoryFollowsMouse(graphics, x0, PREVIEW_TOP, x1, y1, size, 0.0625F, mouseX, mouseY, entity);
		}
		Component name = this.selectedType == null
				? this.minecraft.player != null ? this.minecraft.player.getName() : Component.empty()
				: this.selectedType.getDescription();
		graphics.centeredText(this.font, name, this.width / 2, y1 + 4, 0xFFFFFFFF);
	}

	private void extractGrid(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		graphics.enableScissor(0, this.gridY0, this.width, this.gridY1);
		int yBase = this.gridY0 - (int) this.scrollY;

		for (int i = 0; i < this.filteredTypes.size(); i++) {
			int cx = this.gridX0 + (i % this.columns) * CELL_W;
			int cy = yBase + (i / this.columns) * CELL_H;
			if (cy + CELL_H < this.gridY0 || cy > this.gridY1) {
				continue;
			}

			EntityType<?> type = this.filteredTypes.get(i);
			boolean hovered = isGridHover(mouseX, mouseY, cx, cy);
			if (hovered) {
				graphics.fill(cx, cy, cx + CELL_W, cy + CELL_H, 0x30FFFFFF);
				graphics.setTooltipForNextFrame(type.getDescription(), mouseX, mouseY);
			}
			if (type == this.selectedType) {
				graphics.outline(cx, cy, CELL_W, CELL_H, 0xFF58C554);
			}

			LivingEntity entity = this.previewCache.get(type);
			if (entity != null) {
				int size = previewSize(entity, CELL_H - 8);
				float lookX = hovered ? mouseX : cx + CELL_W / 2.0F;
				float lookY = hovered ? mouseY : cy + CELL_H / 2.0F;
				InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
						cx + 2, cy + 2, cx + CELL_W - 2, cy + CELL_H - 2, size, 0.0F, lookX, lookY, entity);
			}
		}
		graphics.disableScissor();
	}

	private boolean isGridHover(double x, double y, int cx, int cy) {
		return y >= this.gridY0 && y < this.gridY1
				&& x >= cx && x < cx + CELL_W && y >= cy && y < cy + CELL_H;
	}

	private static int previewSize(LivingEntity entity, int boxHeight) {
		return Math.max(6, (int) (boxHeight / (entity.getBbHeight() + 0.7F)));
	}

	private int maxScroll() {
		int rows = (this.filteredTypes.size() + this.columns - 1) / this.columns;
		return Math.max(0, rows * CELL_H - (this.gridY1 - this.gridY0));
	}

	@Override
	public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
		if (y >= this.gridY0 && y < this.gridY1) {
			this.scrollY = Mth.clamp(this.scrollY - scrollY * CELL_H / 2.0, 0.0, maxScroll());
			return true;
		}
		return super.mouseScrolled(x, y, scrollX, scrollY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (super.mouseClicked(event, doubleClick)) {
			return true;
		}
		if (event.button() == 0 && event.y() >= this.gridY0 && event.y() < this.gridY1) {
			int col = ((int) event.x() - this.gridX0) / CELL_W;
			int row = ((int) event.y() - this.gridY0 + (int) this.scrollY) / CELL_H;
			if (col >= 0 && col < this.columns && event.x() >= this.gridX0 && row >= 0) {
				int index = row * this.columns + col;
				if (index < this.filteredTypes.size()) {
					selectType(this.filteredTypes.get(index));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
