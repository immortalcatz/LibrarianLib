package com.teamwizardry.librarianlib.common.base.item

import com.teamwizardry.librarianlib.client.core.JsonGenerationUtils
import com.teamwizardry.librarianlib.client.core.ModelHandler
import com.teamwizardry.librarianlib.common.base.IModelGenerator
import com.teamwizardry.librarianlib.common.base.ModCreativeTab
import com.teamwizardry.librarianlib.common.util.VariantHelper
import com.teamwizardry.librarianlib.common.util.currentModId
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.NonNullList

/**
 * The default implementation for an IVariantHolder sword.
 */
@Suppress("LeakingThis")
open class ItemModSword(name: String, material: ToolMaterial, vararg variants: String) : ItemSword(material), IModItemProvider, IModelGenerator {

    override val providedItem: Item
        get() = this

    private val bareName = VariantHelper.toSnakeCase(name)
    private val modId = currentModId
    override val variants = VariantHelper.setupItem(this, bareName, variants, creativeTab)

    override fun setUnlocalizedName(name: String): Item {
        VariantHelper.setUnlocalizedNameForItem(this, modId, name)
        return super.setUnlocalizedName(name)
    }

    override fun getUnlocalizedName(stack: ItemStack): String {
        val dmg = stack.itemDamage
        val variants = this.variants
        val name = if (dmg >= variants.size) this.bareName else variants[dmg]

        return "item.$modId:$name"
    }

    override fun getSubItems(itemIn: Item, tab: CreativeTabs?, subItems: NonNullList<ItemStack>) {
        variants.indices.mapTo(subItems) { ItemStack(itemIn, 1, it) }
    }

    /**
     * Override this to have a custom creative tab. Leave blank to have a default tab (or none if no default tab is set).
     */
    open val creativeTab: ModCreativeTab?
        get() = ModCreativeTab.defaultTabs[modId]

    // Model Generation

    override fun generateMissingItem(variant: String): Boolean {
        ModelHandler.generateItemJson(this) {
            mapOf(JsonGenerationUtils.getPathForItemModel(this, variant)
                    to JsonGenerationUtils.generateBaseItemModel(this, variant, "item/handheld"))
        }
        return true
    }
}

