#version 140

in vec2 textureCoords;

out vec4 out_colour;

uniform sampler2D originalTexture;
uniform vec2 u_resolution;
uniform vec2 blurDir;

void main(void){
    vec2 pixelSize = blurDir.xy / u_resolution.xy;
    out_colour = vec4(0.0);

    out_colour += texture(originalTexture, textureCoords + pixelSize * -5.) * 0.0093;
    out_colour += texture(originalTexture, textureCoords + pixelSize * -4.) * 0.028002;
    out_colour += texture(originalTexture, textureCoords + pixelSize * -3.) * 0.065984;
    out_colour += texture(originalTexture, textureCoords + pixelSize * -2.) * 0.121703;
    out_colour += texture(originalTexture, textureCoords + pixelSize * -1.) * 0.175713;
    out_colour += texture(originalTexture, textureCoords + pixelSize * 0.) * 0.198596;
    out_colour += texture(originalTexture, textureCoords + pixelSize * 1.) * 0.175713;
    out_colour += texture(originalTexture, textureCoords + pixelSize * 2.) * 0.121703;
    out_colour += texture(originalTexture, textureCoords + pixelSize * 3.) * 0.065984;
    out_colour += texture(originalTexture, textureCoords + pixelSize * 4.) * 0.028002;
    out_colour += texture(originalTexture, textureCoords + pixelSize * 5.) * 0.0093;
    out_colour = vec4(out_colour.rgb, 1.);
}
