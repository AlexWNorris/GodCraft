package net.godcraft.client;

import net.godcraft.GodCraft;
import net.godcraft.menu.AltarOfGodsMenu;
import net.godcraft.util.AttunementUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.List;

public class AltarOfGodsScreen extends AbstractContainerScreen<AltarOfGodsMenu> {
    private static final Identifier TEXTURE_BLESSINGS = Identifier.parse("godcraft:textures/gui/blessings.png");
    private static final Identifier TEXTURE_ATTUNEMENTS = Identifier.parse("godcraft:textures/gui/attuenments.png");
    private static final Identifier MAJOR_SLOT = Identifier.parse("godcraft:textures/gui/major_slot.png");
    private static final Identifier MINOR_SLOT = Identifier.parse("godcraft:textures/gui/minor_slot.png");

    private final java.util.Map<Integer, Identifier> iconCache = new java.util.HashMap<>();

    // Friendly display names for each enchantment (trimmed, title-cased)
    private static final List<String> ENCHANT_DISPLAY_NAMES = buildDisplayNames();

    // Attunements tab paging
    private int currentPage = 0;
    private static final int ENTRIES_PER_PAGE = 8;

    // Colors
    private static final int COLOR_UNLOCKED_BG       = 0xFF1A2A3A;
    private static final int COLOR_EQUIPPED_BG        = 0xFF1A3A1A;
    private static final int COLOR_LOCKED_BG          = 0xFF2A2A2A;
    private static final int COLOR_UNLOCKED_BORDER    = 0xFF4A7FAA;
    private static final int COLOR_EQUIPPED_BORDER    = 0xFF3AAA3A;
    private static final int COLOR_LOCKED_BORDER      = 0xFF555555;
    private static final int COLOR_TEXT_UNLOCKED      = 0xFFCCEEFF;
    private static final int COLOR_TEXT_EQUIPPED      = 0xFF88FF88;
    private static final int COLOR_TEXT_LOCKED        = 0xFF777777;
    private static final int COLOR_TAB_ACTIVE         = 0xFFE8D8A0;
    private static final int COLOR_TAB_INACTIVE       = 0xFF888888;
    private static final int COLOR_EQUIPPED_BADGE     = 0xFF44DD44;

    private int draggedAttunementId = -1;
    private int dragStartX = 0;
    private int dragStartY = 0;
    private int dragMouseX = 0;
    private int dragMouseY = 0;
    private boolean isDragging = false;

    public AltarOfGodsScreen(AltarOfGodsMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private void updateDimensions() {
        if (this.menu.getActiveTab() == 0) {
            this.imageWidth = 176;
            this.imageHeight = 166;
        } else {
            this.imageWidth = 276;
            this.imageHeight = 166;
        }
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Draw background based on selected tab
        if (this.menu.getActiveTab() == 0) {
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE_BLESSINGS, x, y, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);
        } else {
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, TEXTURE_ATTUNEMENTS, x, y, 0f, 0f, this.imageWidth, this.imageHeight, 512, 256);
        }

        // Draw major/minor slot overlays for Blessings tab only
        if (this.menu.getActiveTab() == 0) {
            // Major slot (left of gear slot)
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, MAJOR_SLOT, x + 55, y + 19, 0f, 0f, 20, 19, 20, 19);
            // Minor slot (right of gear slot)
            guiGraphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, MINOR_SLOT, x + 103, y + 19, 0f, 0f, 20, 19, 20, 19);
        }

        int activeTab = this.menu.getActiveTab();

        if (activeTab == 1) {
            renderAttunementsBg(guiGraphics, x, y, mouseX, mouseY);
        }
    }

    private void renderAttunementsBg(GuiGraphics gg, int x, int y, int mouseX, int mouseY) {
        int contentY = y + 16;
        int totalEnchants = AttunementUtil.VANILLA_ENCHANTMENT_IDS.size();

        // Left Panel (Equipped)
        int eqX = x + 8;
        int eqY = y + 16;
        int eqW = 160;
        int eqH = 130;
        
        int iconSize = 24;
        int pad = 4;
        
        int eqCols = eqW / (iconSize + pad);
        int eqCount = 0;
        
        for (int i = 0; i < totalEnchants; i++) {
            if (this.menu.isAttunementEquipped(i)) {
                if (isDragging && draggedAttunementId == i) continue; // Don't draw if dragging

                int col = eqCount % eqCols;
                int row = eqCount / eqCols;
                int drawX = eqX + col * (iconSize + pad);
                int drawY = eqY + row * (iconSize + pad);
                
                boolean hovered = mouseX >= drawX && mouseX < drawX + iconSize && mouseY >= drawY && mouseY < drawY + iconSize;
                int bgColor = hovered ? 0xFF254025 : COLOR_EQUIPPED_BG;
                
                gg.fill(drawX, drawY, drawX + iconSize, drawY + iconSize, bgColor);
                gg.fill(drawX, drawY, drawX + iconSize, drawY + 1, COLOR_EQUIPPED_BORDER);
                gg.fill(drawX, drawY + iconSize - 1, drawX + iconSize, drawY + iconSize, COLOR_EQUIPPED_BORDER);
                gg.fill(drawX, drawY, drawX + 1, drawY + iconSize, COLOR_EQUIPPED_BORDER);
                gg.fill(drawX + iconSize - 1, drawY, drawX + iconSize, drawY + iconSize, COLOR_EQUIPPED_BORDER);

                Identifier iconId = getIconFor(i);
                gg.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, iconId, drawX, drawY, iconSize, iconSize, 0, 0, 16, 16, 16, 16);
                
                eqCount++;
            }
        }

        // Right Panel (Options)
        int optX = x + 184;
        int optY = contentY;
        int optW = 84;
        int optCols = optW / (iconSize + pad);
        
        List<Integer> options = new ArrayList<>();
        for (int i = 0; i < totalEnchants; i++) {
            if (this.menu.isAttunementUnlocked(i) && !this.menu.isAttunementEquipped(i)) {
                options.add(i);
            }
        }
        
        int totalPages = (int) Math.ceil((double) options.size() / ENTRIES_PER_PAGE);
        if (currentPage >= totalPages && totalPages > 0) currentPage = totalPages - 1;
        
        int startIdx = currentPage * ENTRIES_PER_PAGE;
        int endIdx = Math.min(startIdx + ENTRIES_PER_PAGE, options.size());

        for (int i = startIdx; i < endIdx; i++) {
            int attunementId = options.get(i);
            if (isDragging && draggedAttunementId == attunementId) continue; // Don't draw if dragging

            int pageIdx = i - startIdx;
            int col = pageIdx % optCols;
            int row = pageIdx / optCols;
            int drawX = optX + col * (iconSize + pad);
            int drawY = optY + row * (iconSize + pad);

            int bgColor = COLOR_UNLOCKED_BG;
            int borderColor = COLOR_UNLOCKED_BORDER;

            boolean hovered = mouseX >= drawX && mouseX < drawX + iconSize && mouseY >= drawY && mouseY < drawY + iconSize;
            if (hovered) {
                bgColor = 0xFF1F3550;
            }

            gg.fill(drawX, drawY, drawX + iconSize, drawY + iconSize, bgColor);
            gg.fill(drawX, drawY, drawX + iconSize, drawY + 1, borderColor);
            gg.fill(drawX, drawY + iconSize - 1, drawX + iconSize, drawY + iconSize, borderColor);
            gg.fill(drawX, drawY, drawX + 1, drawY + iconSize, borderColor);
            gg.fill(drawX + iconSize - 1, drawY, drawX + iconSize, drawY + iconSize, borderColor);

            Identifier iconId = getIconFor(attunementId);
            gg.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, iconId, drawX, drawY, iconSize, iconSize, 0, 0, 16, 16, 16, 16);
        }

        // Page navigation
        int navY = y + 145;
        String pageText = totalPages == 0 ? "1/1" : (currentPage + 1) + "/" + totalPages;
        int pageTextW = this.font.width(pageText);
        int navCenterX = x + 226;
        gg.drawString(this.font, pageText, navCenterX - pageTextW / 2, navY, 0xFFAA99CC, false);

        if (currentPage > 0) {
            int btnX = navCenterX - pageTextW / 2 - 16;
            gg.fill(btnX, navY - 1, btnX + 13, navY + 9, 0xFF2D2650);
            gg.fill(btnX, navY - 1, btnX + 13, navY, 0xFF8875CC);
            gg.fill(btnX, navY + 8, btnX + 13, navY + 9, 0xFF8875CC);
            gg.drawString(this.font, "<", btnX + 3, navY, 0xFFCCBBFF, false);
        }

        if (currentPage < totalPages - 1) {
            int btnX = navCenterX + pageTextW / 2 + 3;
            gg.fill(btnX, navY - 1, btnX + 13, navY + 9, 0xFF2D2650);
            gg.fill(btnX, navY - 1, btnX + 13, navY, 0xFF8875CC);
            gg.fill(btnX, navY + 8, btnX + 13, navY + 9, 0xFF8875CC);
            gg.drawString(this.font, ">", btnX + 3, navY, 0xFFCCBBFF, false);
        }
        
        // Draw dragged icon floating
        if (isDragging && draggedAttunementId >= 0) {
            Identifier iconId = getIconFor(draggedAttunementId);
            int drawX = dragMouseX - iconSize / 2;
            int drawY = dragMouseY - iconSize / 2;
            gg.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, iconId, drawX, drawY, iconSize, iconSize, 0, 0, 16, 16, 16, 16);
        }
    }

    private Identifier getIconFor(int attunementId) {
        if (iconCache.containsKey(attunementId)) {
            return iconCache.get(attunementId);
        }
        
        String idStr = AttunementUtil.VANILLA_ENCHANTMENT_IDS.get(attunementId);
        String name = idStr.substring(idStr.indexOf(':') + 1);
        Identifier iconId = Identifier.parse("godcraft:textures/gui/enchantment_icons/" + name + ".png");
        
        boolean exists = this.minecraft != null && this.minecraft.getResourceManager().getResource(iconId).isPresent();
        
        GodCraft.LOGGER.debug("Checking icon for {}: {} -> exists: {}", idStr, iconId, exists);
        
        if (exists) {
            iconCache.put(attunementId, iconId);
            return iconId;
        } else {
            iconCache.put(attunementId, MINOR_SLOT);
            return MINOR_SLOT;
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.updateDimensions();
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        // Draw title
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.drawString(this.font, "Altar of the Gods", x + 8, y + 5, 0xFFE8D8A0, false);

        // Inventory label
        if (this.menu.getActiveTab() == 0) {
            guiGraphics.drawString(this.font, this.playerInventoryTitle, x + this.inventoryLabelX, y + this.inventoryLabelY, 0xFFAA99CC, false);
        }

        // Draw attunement tooltips in Attunements tab
        if (this.menu.getActiveTab() == 1) {
            renderAttunementTooltips(guiGraphics, x, y, mouseX, mouseY);
        }
    }

    private void renderAttunementTooltips(GuiGraphics gg, int x, int y, int mouseX, int mouseY) {
        if (isDragging) return; // Hide tooltips while dragging
        
        int contentY = y + 16;
        int totalEnchants = AttunementUtil.VANILLA_ENCHANTMENT_IDS.size();
        
        int iconSize = 24;
        int pad = 4;

        // Left Panel (Equipped)
        int eqX = x + 8;
        int eqY = y + 16;
        int eqW = 160;
        int eqCols = eqW / (iconSize + pad);
        int eqCount = 0;
        
        for (int i = 0; i < totalEnchants; i++) {
            if (this.menu.isAttunementEquipped(i)) {
                int col = eqCount % eqCols;
                int row = eqCount / eqCols;
                int drawX = eqX + col * (iconSize + pad);
                int drawY = eqY + row * (iconSize + pad);
                
                if (mouseX >= drawX && mouseX < drawX + iconSize && mouseY >= drawY && mouseY < drawY + iconSize) {
                    drawTooltip(gg, mouseX, mouseY, i, true, true);
                    return;
                }
                eqCount++;
            }
        }

        // Right Panel (Options)
        int optX = x + 184;
        int optY = contentY;
        int optW = 84;
        int optCols = optW / (iconSize + pad);
        
        List<Integer> options = new ArrayList<>();
        for (int i = 0; i < totalEnchants; i++) {
            if (this.menu.isAttunementUnlocked(i) && !this.menu.isAttunementEquipped(i)) {
                options.add(i);
            }
        }
        
        int startIdx = currentPage * ENTRIES_PER_PAGE;
        int endIdx = Math.min(startIdx + ENTRIES_PER_PAGE, options.size());

        for (int i = startIdx; i < endIdx; i++) {
            int attunementId = options.get(i);
            int pageIdx = i - startIdx;
            int col = pageIdx % optCols;
            int row = pageIdx / optCols;
            int drawX = optX + col * (iconSize + pad);
            int drawY = optY + row * (iconSize + pad);

            if (mouseX >= drawX && mouseX < drawX + iconSize && mouseY >= drawY && mouseY < drawY + iconSize) {
                drawTooltip(gg, mouseX, mouseY, attunementId, true, false);
                return;
            }
        }
    }
    
    private void drawTooltip(GuiGraphics gg, int mouseX, int mouseY, int attunementId, boolean unlocked, boolean equipped) {
        String fullName = ENCHANT_DISPLAY_NAMES.get(attunementId);
        String id = AttunementUtil.VANILLA_ENCHANTMENT_IDS.get(attunementId);

        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.literal(fullName).withStyle(
                equipped ? net.minecraft.ChatFormatting.GREEN :
                (unlocked ? net.minecraft.ChatFormatting.AQUA : net.minecraft.ChatFormatting.DARK_GRAY)));
        tooltip.add(Component.literal(id).withStyle(net.minecraft.ChatFormatting.DARK_GRAY));
        if (!unlocked) {
            tooltip.add(Component.literal("Locked").withStyle(net.minecraft.ChatFormatting.RED));
        } else if (equipped) {
            tooltip.add(Component.literal("Drag to unequip").withStyle(net.minecraft.ChatFormatting.GRAY));
        } else {
            tooltip.add(Component.literal("Drag to equip").withStyle(net.minecraft.ChatFormatting.GRAY));
        }

        int tooltipX = mouseX + 4;
        int tooltipY = mouseY;
        int tooltipW = 0;
        for (Component line : tooltip) {
            tooltipW = Math.max(tooltipW, this.font.width(line));
        }
        tooltipW += 8;
        int tooltipH = tooltip.size() * (this.font.lineHeight + 1) + 4;
        if (tooltipX + tooltipW > this.width) tooltipX = mouseX - tooltipW - 4;
        if (tooltipY + tooltipH > this.height) tooltipY = this.height - tooltipH;
        gg.fill(tooltipX - 1, tooltipY - 1, tooltipX + tooltipW + 1, tooltipY + tooltipH + 1, 0xFF000000);
        gg.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + tooltipH, 0xEE1A1A2E);
        gg.fill(tooltipX, tooltipY, tooltipX + tooltipW, tooltipY + 1, 0xFF8875CC);
        gg.fill(tooltipX, tooltipY + tooltipH - 1, tooltipX + tooltipW, tooltipY + tooltipH, 0xFF8875CC);
        for (int ti = 0; ti < tooltip.size(); ti++) {
            gg.drawString(this.font, tooltip.get(ti), tooltipX + 4, tooltipY + 2 + ti * (this.font.lineHeight + 1), 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        this.updateDimensions();
        double mouseX = event.x();
        double mouseY = event.y();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // Tab click detection
        // Left tab is around x + 60 to x + 110, y = 0 to 20
        // Tab click detection
        if (mouseY >= y && mouseY < y + 20) {
            if (this.menu.getActiveTab() == 0) {
                // In Blessings (0), tabs are on the left side. Left tab is Grey -> switch to Attunements (1)
                if (mouseX >= x + 60 && mouseX <= x + 110) {
                    this.sendMenuButton(1);
                    currentPage = 0;
                    return true;
                }
            } else if (this.menu.getActiveTab() == 1) {
                // In Attunements (1), tabs are shifted to the right by 100 pixels.
                // Right tab is Grey -> switch to Blessings (0)
                if (mouseX >= x + 210 && mouseX <= x + 260) {
                    this.sendMenuButton(0);
                    currentPage = 0;
                    return true;
                }
            }
        }

        // Attunements tab clicks
        if (this.menu.getActiveTab() == 1) {
            int contentY = y + 16;
            int totalEnchants = AttunementUtil.VANILLA_ENCHANTMENT_IDS.size();
            
            int iconSize = 24;
            int pad = 4;

            // Left Panel (Equipped)
            int eqX = x + 8;
            int eqY = y + 16;
            int eqW = 160;
            int eqCols = eqW / (iconSize + pad);
            int eqCount = 0;
            
            for (int i = 0; i < totalEnchants; i++) {
                if (this.menu.isAttunementEquipped(i)) {
                    int col = eqCount % eqCols;
                    int row = eqCount / eqCols;
                    int drawX = eqX + col * (iconSize + pad);
                    int drawY = eqY + row * (iconSize + pad);
                    
                    if (mouseX >= drawX && mouseX < drawX + iconSize && mouseY >= drawY && mouseY < drawY + iconSize) {
                        isDragging = true;
                        draggedAttunementId = i;
                        dragStartX = (int)mouseX;
                        dragStartY = (int)mouseY;
                        dragMouseX = (int)mouseX;
                        dragMouseY = (int)mouseY;
                        return true;
                    }
                    eqCount++;
                }
            }

            // Right Panel (Options)
            int optX = x + 184;
            int optY = contentY;
            int optW = 84;
            int optCols = optW / (iconSize + pad);
            
            List<Integer> options = new ArrayList<>();
            for (int i = 0; i < totalEnchants; i++) {
                if (this.menu.isAttunementUnlocked(i) && !this.menu.isAttunementEquipped(i)) {
                    options.add(i);
                }
            }
            
            int totalPages = (int) Math.ceil((double) options.size() / ENTRIES_PER_PAGE);
            int startIdx = currentPage * ENTRIES_PER_PAGE;
            int endIdx = Math.min(startIdx + ENTRIES_PER_PAGE, options.size());

            for (int i = startIdx; i < endIdx; i++) {
                int attunementId = options.get(i);
                int pageIdx = i - startIdx;
                int col = pageIdx % optCols;
                int row = pageIdx / optCols;
                int drawX = optX + col * (iconSize + pad);
                int drawY = optY + row * (iconSize + pad);

                if (mouseX >= drawX && mouseX < drawX + iconSize && mouseY >= drawY && mouseY < drawY + iconSize) {
                    isDragging = true;
                    draggedAttunementId = attunementId;
                    dragStartX = (int)mouseX;
                    dragStartY = (int)mouseY;
                    dragMouseX = (int)mouseX;
                    dragMouseY = (int)mouseY;
                    return true;
                }
            }

            // Page navigation buttons
            int navY = y + 145;
            String pageText = totalPages == 0 ? "1/1" : (currentPage + 1) + "/" + totalPages;
            int pageTextW = this.font.width(pageText);
            int navCenterX = x + 226;

            // < button
            if (currentPage > 0) {
                int btnX = navCenterX - pageTextW / 2 - 16;
                if (mouseX >= btnX && mouseX < btnX + 13 && mouseY >= navY - 1 && mouseY < navY + 9) {
                    currentPage--;
                    return true;
                }
            }
            // > button
            if (currentPage < totalPages - 1) {
                int btnX = navCenterX + pageTextW / 2 + 3;
                if (mouseX >= btnX && mouseX < btnX + 13 && mouseY >= navY - 1 && mouseY < navY + 9) {
                    currentPage++;
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }
    
    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (isDragging) {
            this.dragMouseX = (int)event.x();
            this.dragMouseY = (int)event.y();
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDragging) {
            isDragging = false;
            
            this.updateDimensions();
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            
            // Left Panel (Equipped bounds)
            int eqX = x + 8;
            int eqY = y + 16;
            int eqW = 160;
            int eqH = 130;
            
            double mouseX = event.x();
            double mouseY = event.y();
            
            boolean droppedInLeftPanel = mouseX >= eqX && mouseX < eqX + eqW && mouseY >= eqY && mouseY < eqY + eqH;
            boolean currentlyEquipped = this.menu.isAttunementEquipped(draggedAttunementId);

            if (droppedInLeftPanel && !currentlyEquipped) {
                // Equip
                this.sendMenuButton(100 + draggedAttunementId);
            } else if (!droppedInLeftPanel && currentlyEquipped) {
                // Unequip
                this.sendMenuButton(100 + draggedAttunementId);
            }
            
            draggedAttunementId = -1;
            return true;
        }
        return super.mouseReleased(event);
    }

    /** Sends a menu button click packet to the server. */
    private void sendMenuButton(int id) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    /** Builds friendly display names from enchantment IDs (e.g. "minecraft:feather_falling" → "Feather Falling"). */
    private static List<String> buildDisplayNames() {
        List<String> names = new ArrayList<>();
        for (String id : AttunementUtil.VANILLA_ENCHANTMENT_IDS) {
            String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
            String[] words = path.split("_");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (!sb.isEmpty()) sb.append(' ');
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1));
            }
            names.add(sb.toString());
        }
        return names;
    }
}
