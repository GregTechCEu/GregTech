#version 120

attribute vec2 position;

varying  vec2 textureCoords;

void main(void){
    gl_Position = vec4(position, 0.0, 1.0);
    textureCoords = position * 0.5 + 0.5;
}
