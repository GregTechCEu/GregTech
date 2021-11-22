#version 120

varying vec2 textureCoords;

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
    gl_FragColor = bloomStrength * ( lerpBloomFactor(1.) * texture2D(blurTexture1, textureCoords) +
    lerpBloomFactor(0.8) * texture2D(blurTexture2, textureCoords) +
    lerpBloomFactor(0.6) * texture2D(blurTexture3, textureCoords) +
    lerpBloomFactor(0.4) * texture2D(blurTexture4, textureCoords) +
    lerpBloomFactor(0.2) * texture2D(blurTexture5, textureCoords) );
}
