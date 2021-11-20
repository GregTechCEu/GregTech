#version 140

in vec2 textureCoords;

out vec4 out_colour;

uniform sampler2D buffer_a;
uniform sampler2D buffer_b;
uniform float intensive;

void main(void){
    out_colour = vec4(texture(buffer_a, textureCoords).rgb + texture(buffer_b, textureCoords).rgb * intensive, 1.);
}
