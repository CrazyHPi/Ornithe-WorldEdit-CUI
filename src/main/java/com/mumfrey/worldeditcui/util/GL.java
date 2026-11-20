/*
 * This file is part of LiteLoader.
 * Copyright (C) 2012-16 Adam Mummery-Smith
 * All Rights Reserved.
 */
package com.mumfrey.worldeditcui.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

/**
 * <p>Edit: This class now only contains NEEDED GL things for this mod.
 * No longer public API.
 * </p>
 * <p>
 * Convenience class for working with Mojang's GLStateManager:
 *
 * <p>It would be pretty tolerable to work with GLStateManager as a static
 * import were it not for the fact that you still need to import the GL
 * namespaces themselves from LWJGL in order to get the constants, and also have
 * to deal with the fact that GLStateManager's methods don't have "gl-style"
 * names, making it annoying to work with. This class is designed to function as
 * an adapter to allow changeover to be more painless. Using this class means
 * that the following code:</p>
 *
 * <pre><code>glEnable(GL_BLEND);
 * glAlphaFunc(GL_GREATER, 0.0F);</code></pre>
 *
 * <p>becomes:</p>
 *
 * <pre><code>glEnableBlend();
 * glAlphaFunc(GL_GREATER, 0.0F);</code></pre>
 *
 * <p>Notice that the <tt>glAlphaFunc</tt> invocation remains unchanged, and the
 * <tt>glEnable</tt> call simply gets replaced with a logical equivalent which
 * invokes the GLStateManager method behind the scenes.</p>
 *
 * <p>To use this class, simply replace existing static imports in your classes
 * with this single static import, then change <tt>glEnable</tt> and <tt>
 * glDisable</tt> calls accordingly.
 *
 * @author Adam Mummery-Smith
 */

public class GL {
    // Vertex Formats
    public static final VertexFormat VF_POSITION = DefaultVertexFormats.POSITION;
    public static final VertexFormat VF_POSITION_TEX = DefaultVertexFormats.POSITION_TEX;


    public static final int GL_LESS = 0x201;
    public static final int GL_LEQUAL = 0x203;
    public static final int GL_GREATER = 0x204;
    public static final int GL_GEQUAL = 0x206;
    public static final int GL_ALWAYS = 0x207;
    public static final int GL_LINES = 0x1;
    public static final int GL_LINE_LOOP = 0x2;
    public static final int GL_TRIANGLES = 0x4;
    public static final int GL_QUADS = 0x7;
    public static final int GL_SRC_ALPHA = 0x302;
    public static final int GL_ONE_MINUS_SRC_ALPHA = 0x303;
    public static final int GL_OR_REVERSE = 0x150b;

    public static void glEnableAlphaTest() {
        GlStateManager.enableAlpha();
    }

    public static void glAlphaFunc(int func, float ref) {
        GlStateManager.alphaFunc(func, ref);
    }

    public static void glDisableLighting() {
        GlStateManager.disableLighting();
    }

    public static void glEnableDepthTest() {
        GlStateManager.enableDepth();
    }

    public static void glDepthFunc(int func) {
        GlStateManager.depthFunc(func);
    }

    public static void glDepthMask(boolean flag) {
        GlStateManager.depthMask(flag);
    }

    public static void glDisableBlend() {
        GlStateManager.disableBlend();
    }

    public static void glEnableBlend() {
        GlStateManager.enableBlend();
    }

    public static void glBlendFunc(int sfactor, int dfactor) {
        GlStateManager.blendFunc(sfactor, dfactor);
    }

    public static void glDisableFog() {
        GlStateManager.disableFog();
    }

    public static void glEnableCulling() {
        GlStateManager.enableCull();
    }

    public static void glDisableCulling() {
        GlStateManager.disableCull();
    }

    public static void glEnableColorLogic() {
        GlStateManager.enableColorLogic();
    }

    public static void glDisableColorLogic() {
        GlStateManager.disableColorLogic();
    }

    public static void glLogicOp(int opcode) {
        GlStateManager.colorLogicOp(opcode);
    }

    public static void glEnableTexture2D() {
        GlStateManager.enableTexture2D();
    }

    public static void glDisableTexture2D() {
        GlStateManager.disableTexture2D();
    }

    public static void glPushMatrix() {
        GlStateManager.pushMatrix();
    }

    public static void glPopMatrix() {
        GlStateManager.popMatrix();
    }

    public static void glRotatef(float angle, float x, float y, float z) {
        GlStateManager.rotate(angle, x, y, z);
    }

    public static void glTranslatef(float x, float y, float z) {
        GlStateManager.translate(x, y, z);
    }

    public static void glTranslated(double x, double y, double z) {
        GlStateManager.translate(x, y, z);
    }

    public static void glColor4f(float red, float green, float blue, float alpha) {
        GlStateManager.color(red, green, blue, alpha);
    }

    public static void glLineWidth(float width) {
        GlStateManager.glLineWidth(width);
    }

}