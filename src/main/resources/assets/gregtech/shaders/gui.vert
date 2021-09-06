#version 120

varying vec2 textureCoords;

void main()
{
    gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    textureCoords = position.xy * 0.5 + 0.5;
}
