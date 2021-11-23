#version 120

varying vec2 textureCoords;


uniform sampler2D buffer_a;
uniform sampler2D buffer_b;
uniform float intensive;

void main(void){
    gl_FragColor = vec4(texture2D(buffer_a, textureCoords).rgb + texture2D(buffer_b, textureCoords).rgb * intensive, 1.);
}
