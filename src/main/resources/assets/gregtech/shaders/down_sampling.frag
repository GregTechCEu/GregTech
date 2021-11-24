#version 120

varying vec2 textureCoords;

uniform sampler2D originalTexture;
uniform vec2 u_resolution;
uniform vec2 u_resolution2;

vec4 four_k(vec3 textel, vec2 uv) {
    return (texture2D(originalTexture, uv + textel.xx) //1 1
    + texture2D(originalTexture, uv + textel.xy) // 1 -1
    + texture2D(originalTexture, uv + textel.yx) // -1 1
    + texture2D(originalTexture, uv + textel.yy)) * 0.25; // -1 -1
}

void main(void) {
    vec3 textel1 = vec3(1., -1., 0.) / u_resolution2.xyx;
    vec3 textel2 = vec3(1., -1., 0.) / u_resolution.xyx;

    vec4 out_colour = (four_k(textel1, textureCoords + textel2.yy)
    + four_k(textel1, textureCoords + textel2.zy)
    + four_k(textel1, textureCoords + textel2.yz)
    + four_k(textel1, textureCoords)) * 0.25 * 0.125;

    out_colour += (four_k(textel1, textureCoords + textel2.xy)
    + four_k(textel1, textureCoords + textel2.zy)
    + four_k(textel1, textureCoords + textel2.xz)
    + four_k(textel1, textureCoords)) * 0.25 * 0.125;

    out_colour += (four_k(textel1, textureCoords + textel2.yx)
    + four_k(textel1, textureCoords + textel2.yz)
    + four_k(textel1, textureCoords + textel2.zx)
    + four_k(textel1, textureCoords)) * 0.25 * 0.125;

    out_colour += (four_k(textel1, textureCoords + textel2.xx)
    + four_k(textel1, textureCoords + textel2.xz)
    + four_k(textel1, textureCoords + textel2.zx)
    + four_k(textel1, textureCoords)) * 0.25 * 0.125;

    out_colour += (four_k(textel1, textureCoords + textel1.xx)
    + four_k(textel1, textureCoords + textel1.xy)
    + four_k(textel1, textureCoords + textel1.yx)
    + four_k(textel1, textureCoords + textel1.yy)) * 0.25 * 0.5;

    gl_FragColor = vec4(out_colour.rgb, 1.);
}
