#version 130

#define PI 3.14159265359
#define TWO_PI 6.28318530718

#define colorRange 24.0

uniform sampler2D texture;
uniform vec2 u_resolution;

in vec2 textureCoords;

vec3 getTexture(vec2 uv){
    vec4 textureSample = texture2D(texture, uv);
    return sqrt(textureSample.rgb * textureSample.a);
}

void main(){
    vec3 color = pow(getTexture(textureCoords), vec3(2.2)) * 10.0;
    gl_FragColor = vec4(pow(color, vec3(1.0 / 2.2)) / colorRange, 1.0);
}
