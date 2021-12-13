#version 120

varying vec2 textureCoords;

uniform sampler2D upTexture;
uniform sampler2D downTexture;
uniform vec2 u_resolution;
uniform vec2 u_resolution2;

vec4 four_k(vec3 textel, vec2 uv) {
    return (texture2D(upTexture, uv + textel.xx) //1 1
    + texture2D(upTexture, uv + textel.xy) // 1 -1
    + texture2D(upTexture, uv + textel.yx) // -1 1
    + texture2D(upTexture, uv + textel.yy)) * 0.25; // -1 -1
}

vec4 up_sampling(vec3 textel, vec2 uv) {
    return vec4(four_k(textel, uv).rgb + texture2D(downTexture, uv).rgb, 1.);
}

void main(void) {
    vec3 textel = vec3(1., -1., 0.) / u_resolution.xyx;
//    out_colour = up_sampling(textel, textureCoords);

    vec4 out_colour = texture2D(upTexture, textureCoords + textel.xx);
    out_colour += texture2D(upTexture, textureCoords + textel.xz) * 2.0;
    out_colour += texture2D(upTexture, textureCoords + textel.xy);
    out_colour += texture2D(upTexture, textureCoords + textel.yz) * 2.0;
    out_colour += texture2D(upTexture, textureCoords) * 4.0;
    out_colour += texture2D(upTexture, textureCoords + textel.zx) * 2.0;
    out_colour += texture2D(upTexture, textureCoords + textel.yy);
    out_colour += texture2D(upTexture, textureCoords + textel.zy) * 2.0;
    out_colour += texture2D(upTexture, textureCoords + textel.yx);

    gl_FragColor = vec4(out_colour.rgb * 0.8 / 16. + texture2D(downTexture, textureCoords).rgb * 0.8, 1.);
}
