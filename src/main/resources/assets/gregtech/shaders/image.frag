#version 120

varying vec2 textureCoords;

uniform sampler2D colourTexture;

void main(void){

    gl_FragColor = texture2D(colourTexture, textureCoords).rgba;

}
