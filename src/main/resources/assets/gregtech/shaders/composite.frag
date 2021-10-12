#version 140

in vec2 textureCoords;

out vec4 out_colour;

uniform sampler2D blurTexture1;
uniform sampler2D blurTexture2;
uniform sampler2D blurTexture3;
uniform sampler2D blurTexture4;
uniform sampler2D blurTexture5;
uniform float bloomStrength;
uniform float bloomRadius;
//uniform float bloomFactors[NUM_MIPS];
//uniform vec3 bloomTintColors[NUM_MIPS];

float lerpBloomFactor(const in float factor) {
    float mirrorFactor = 1.2 - factor;
    return mix(factor, mirrorFactor, bloomRadius);
}
void main() {
    out_colour = bloomStrength * ( lerpBloomFactor(1.) * vec4(1., 1., 1., 1.0) * texture(blurTexture1, textureCoords) +
    lerpBloomFactor(0.8) * vec4(1., 1., 1., 1.0) * texture(blurTexture2, textureCoords) +
    lerpBloomFactor(0.6) * vec4(1., 1., 1., 1.0) * texture(blurTexture3, textureCoords) +
    lerpBloomFactor(0.4) * vec4(1., 1., 1., 1.0) * texture(blurTexture4, textureCoords) +
    lerpBloomFactor(0.2) * vec4(1., 1., 1., 1.0) * texture(blurTexture5, textureCoords) );
}
