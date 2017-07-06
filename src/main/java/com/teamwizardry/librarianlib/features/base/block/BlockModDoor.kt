package com.teamwizardry.librarianlib.features.base.block

import com.teamwizardry.librarianlib.core.client.ModelHandler
import com.teamwizardry.librarianlib.features.base.IModelGenerator
import com.teamwizardry.librarianlib.features.base.ModCreativeTab
import com.teamwizardry.librarianlib.features.helpers.VariantHelper
import com.teamwizardry.librarianlib.features.helpers.currentModId
import com.teamwizardry.librarianlib.features.kotlin.json
import com.teamwizardry.librarianlib.features.utilities.JsonGenerationUtils
import net.minecraft.block.Block
import net.minecraft.block.BlockDoor
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.entity.Entity
import net.minecraft.item.Item
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemDoor
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.world.Explosion
import net.minecraft.world.World
import java.util.*

/**
 * The default implementation for an IModBlock.
 */
@Suppress("LeakingThis")
open class BlockModDoor(name: String, val parent: IBlockState) : BlockDoor(parent.material), IModBlock, IModelGenerator {

    private val parentName = parent.block.registryName

    override val variants: Array<out String>

    override val bareName: String = VariantHelper.toSnakeCase(name)
    val modId = currentModId

    val itemForm: ItemBlock? by lazy { createItemForm() }

    init {
        this.variants = VariantHelper.beginSetupBlock(name, arrayOf())
        VariantHelper.finishSetupBlock(this, bareName, itemForm, this::creativeTab)
    }

    override fun setUnlocalizedName(name: String): Block {
        super.setUnlocalizedName(name)
        VariantHelper.setUnlocalizedNameForBlock(this, modId, name, itemForm)
        return this
    }

    /**
     * Override this to have a custom ItemBlock implementation.
     */
    open fun createItemForm(): ItemBlock? {
        return ItemModDoor(this)
    }

    /**
     * Override this to have a custom creative tab. Leave blank to have a default tab (or none if no default tab is set).
     */
    override val creativeTab: ModCreativeTab?
        get() = ModCreativeTab.defaultTabs[modId]

    override fun getItemDropped(state: IBlockState, rand: Random, fortune: Int): Item? {
        return if (state.getValue(HALF) == BlockDoor.EnumDoorHalf.UPPER) null else this.itemForm
    }

    override fun getExplosionResistance(world: World, pos: BlockPos, exploder: Entity?, explosion: Explosion) = parent.block.getExplosionResistance(world, pos, exploder, explosion)
    override fun getBlockHardness(blockState: IBlockState, worldIn: World, pos: BlockPos) = parent.getBlockHardness(worldIn, pos)
    override fun isToolEffective(type: String?, state: IBlockState) = parent.block.isToolEffective(type, parent)
    override fun getHarvestTool(state: IBlockState): String? = parent.block.getHarvestTool(parent)

    override fun generateMissingBlockstate(mapper: ((Block) -> Map<IBlockState, ModelResourceLocation>)?): Boolean {
        val name = ResourceLocation(parentName!!.resourceDomain, "blocks/${parentName!!.resourcePath}").toString()
        val simpleName = registryName!!.resourcePath

        ModelHandler.generateBlockJson(this, {
            JsonGenerationUtils.generateBlockStates(this, mapper) {
                val suffix = (if ("half=lower" in it) "bottom" else "top") + if (("open=true" in it) xor ("hinge=left" in it)) "_rh" else ""
                var rot = if ("facing=east" in it) 0 else if ("facing=south" in it) 90 else if ("facing=west" in it) 180 else 270
                if ("hinge=left" in it && "open=true" in it) rot += 90
                else if ("hinge=right" in it && "open=true" in it) rot += 270
                rot %= 360
                json {
                    obj(
                            "model" to "${registryName}_$suffix",
                            *(if (rot == 0) arrayOf() else arrayOf("y" to rot))
                    )
                }
            }
        }, {
            mapOf(JsonGenerationUtils.getPathForBlockModel(this, "${simpleName}_bottom")
                    to json {
                obj(
                        "parent" to "block/door_bottom",
                        "textures" to obj(
                                "bottom" to name + "_door_lower",
                                "top" to name + "_door_upper"
                        )
                )
            },
                    JsonGenerationUtils.getPathForBlockModel(this, "${simpleName}_top")
                            to json {
                        obj(
                                "parent" to "block/door_top",
                                "textures" to obj(
                                        "bottom" to name + "_door_lower",
                                        "top" to name + "_door_upper"
                                )
                        )
                    },
                    JsonGenerationUtils.getPathForBlockModel(this, "${simpleName}_bottom_rh")
                            to json {
                        obj(
                                "parent" to "block/door_bottom_rh",
                                "textures" to obj(
                                        "bottom" to name + "_door_lower",
                                        "top" to name + "_door_upper"
                                )
                        )
                    },
                    JsonGenerationUtils.getPathForBlockModel(this, "${simpleName}_top_rh")
                            to json {
                        obj(
                                "parent" to "block/door_top_rh",
                                "textures" to obj(
                                        "bottom" to name + "_door_lower",
                                        "top" to name + "_door_upper"
                                )
                        )
                    })
        })
        return true
    }
}
