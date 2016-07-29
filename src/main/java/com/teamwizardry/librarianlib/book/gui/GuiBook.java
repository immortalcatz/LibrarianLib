package com.teamwizardry.librarianlib.book.gui;

import java.util.List;

import net.minecraftforge.fml.client.config.GuiUtils;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ImmutableList;
import com.teamwizardry.librarianlib.LibrarianLib;
import com.teamwizardry.librarianlib.api.gui.GuiBase;
import com.teamwizardry.librarianlib.api.gui.components.ComponentRaw;
import com.teamwizardry.librarianlib.api.gui.components.ComponentSprite;
import com.teamwizardry.librarianlib.api.gui.components.ComponentText;
import com.teamwizardry.librarianlib.api.gui.components.ComponentText.TextAlignH;
import com.teamwizardry.librarianlib.api.gui.components.ComponentText.TextAlignV;
import com.teamwizardry.librarianlib.api.gui.components.ComponentVoid;
import com.teamwizardry.librarianlib.api.gui.components.mixin.ButtonMixin;
import com.teamwizardry.librarianlib.api.gui.components.mixin.ScissorMixin;
import com.teamwizardry.librarianlib.api.util.gui.ScissorUtil;
import com.teamwizardry.librarianlib.api.util.misc.Color;
import com.teamwizardry.librarianlib.api.util.misc.PathUtils;
import com.teamwizardry.librarianlib.book.Book;
import com.teamwizardry.librarianlib.book.util.Page;
import com.teamwizardry.librarianlib.client.Sprite;
import com.teamwizardry.librarianlib.client.Texture;
import com.teamwizardry.librarianlib.common.network.data.DataNode;
import com.teamwizardry.librarianlib.math.Vec2;

public class GuiBook extends GuiBase {

	public static final int PAGE_WIDTH = 120, PAGE_HEIGHT = 161;
	public static Texture TEXTURE = new Texture(new ResourceLocation(LibrarianLib.MODID, "textures/book/book.png"));
	public static Sprite BOOK_BACKGROUND_BORDER = TEXTURE.getSprite("background_border", 146, 180);
	public static Sprite BACKGROUND_PAGE = TEXTURE.getSprite("background_page", 146, 180);
	public static Sprite TITLE_BAR = TEXTURE.getSprite("title_bar", 133, 13);
	public static Sprite BOOKMARK = TEXTURE.getSprite("bookmark", 100, 13);
	public static Sprite BACK_PAGE = TEXTURE.getSprite("back_page", 18, 10);
	public static Sprite NEXT_PAGE = TEXTURE.getSprite("next_page", 18, 10);
	public static Sprite BACK_ARROW = TEXTURE.getSprite("back_arrow", 18, 9);
	public static Sprite UP_ARROW = TEXTURE.getSprite("up_arrow", 9, 18);
	public static Sprite DOWN_ARROW = TEXTURE.getSprite("down_arrow", 9, 18);
	public static Sprite CHECKBOX = TEXTURE.getSprite("checkbox", 9, 9);
	public static Sprite CHECKBOX_ON = TEXTURE.getSprite("checkbox_on", 9, 9);
	public static Sprite CHECKMARK = TEXTURE.getSprite("checkmark", 16, 16);
	public static Sprite SLIDER_NORMAL = TEXTURE.getSprite("slider_normal", 133, 37);
	public static Sprite SLIDER_RECIPE = TEXTURE.getSprite("slider_recipe", 133, 68);
	
	public final Book book;
	public final Page page;
	protected final DataNode rootData;
	protected final DataNode pageData;
	protected ComponentVoid contents, tips;
	
	public GuiBook(Book book, DataNode rootData, DataNode pageData, Page page) {
		super(146, 180);
		
		this.book = book;
		this.page = page;

		this.rootData = rootData;
		this.pageData = pageData;
		
		// title bar
		ComponentVoid titleBar = new ComponentVoid((BACKGROUND_PAGE.width-TITLE_BAR.width)/2, -19, TITLE_BAR.width, TITLE_BAR.height);
		titleBar.add(new ComponentSprite(TITLE_BAR, 0, 0));
		titleBar.add(new ComponentText(66, 7, TextAlignH.CENTER, TextAlignV.MIDDLE).val("TITLE"));
		
		// nav
		ComponentVoid navBar = new ComponentVoid((BACKGROUND_PAGE.width-TITLE_BAR.width)/2, 186, TITLE_BAR.width, TITLE_BAR.height);
		
		Color
        disabledColor = Color.rgb(0xB0B0B0),
        hoverColor = Color.rgb(0x0DDED3),
        normalColor = Color.rgb(0x0DBFA2);
		
		navBar.add(new ComponentSprite(TITLE_BAR, 0, 0));
		navBar.add(new ComponentSprite(BACK_PAGE, 15, 2).setup((b) -> {
			b.setEnabled(pageData.get("hasPrev").exists());
			new ButtonMixin(b,
					() -> {
						b.color.setValue(normalColor);
					}, () -> {
						b.color.setValue(hoverColor);
					}, () -> {
						b.color.setValue(disabledColor);
					},
					() -> {
						openPage(this.page.path, this.page.page-1);
					}
			);
		}));
		navBar.add(new ComponentSprite(NEXT_PAGE, TITLE_BAR.width-NEXT_PAGE.width-15, 2).setup((b) -> {
			b.setEnabled(pageData.get("hasNext").exists());
			new ButtonMixin(b,
					() -> {
						b.color.setValue(normalColor);
					}, () -> {
						b.color.setValue(hoverColor);
					}, () -> {
						b.color.setValue(disabledColor);
					},
					() -> {
						openPage(this.page.path, this.page.page+1);
					}
			);
		}));
		navBar.add(new ComponentSprite(BACK_ARROW, (TITLE_BAR.width / 2) - (BACK_ARROW.width / 2), 2).setup((b) -> {
			b.setEnabled(book.history.size() > 0);
			new ButtonMixin(b,
					() -> {
						b.color.setValue(normalColor);
					}, () -> {
						b.color.setValue(hoverColor);
					}, () -> {
						b.color.setValue(disabledColor);
					},
					() -> {
						book.back();
					}
			);
		}));

		// page
		ComponentVoid contents = new ComponentVoid(13, 9, PAGE_WIDTH, PAGE_HEIGHT);
		ScissorMixin.scissor(contents);
		
		// bg/fg
		ComponentSprite border = new ComponentSprite(BOOK_BACKGROUND_BORDER, 0, 0).setup((b) -> {
			b.depth.setValue(false);
		});
		
		ComponentSprite pageBG = new ComponentSprite(BACKGROUND_PAGE, 0, 0);
		
		tips = new ComponentVoid(0, 0, 0, PAGE_HEIGHT);
		
		tips.zIndex = -100;
		pageBG.zIndex = -5;
		contents.zIndex = 0;
		titleBar.zIndex = 9;
		navBar.zIndex = 9;
		border.zIndex = -10;
		
		components.add(tips);
		components.add(pageBG);
		components.add(border);
		components.add(titleBar);
		if(book.history.size() > 1 || pageData.get("hasNext").exists() || pageData.get("hasPrev").exists())
			components.add(navBar);
		components.add(contents);
		
		this.contents = contents;
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	public void openPageRelative(String path, int page) {
        openPage(PathUtils.resolve(PathUtils.parent(this.page.path), path), page);
    }

    public void openPage(String path, int page) {
    	book.display(new Page(path, page));
    }

    public ResourceLocation pageResource(String path) {
        return new ResourceLocation(book.modid, PathUtils.resolve("textures/" + PathUtils.resolve(PathUtils.parent(this.page.path), path)));
    }
	
}