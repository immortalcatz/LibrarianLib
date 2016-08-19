package com.teamwizardry.librarianlib.common.util.math.shapes

import com.teamwizardry.librarianlib.common.util.math.Vec2d
import java.util.*

/**
 * Created by Saad on 2/7/2016.
 */
interface IShape2D {

    /**
     * Will return a list of points in order that define every point of the helix

     * @return Will return the list of points required
     */
    val points: ArrayList<Vec2d>
}