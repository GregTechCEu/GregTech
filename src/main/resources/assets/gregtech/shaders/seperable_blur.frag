#version 140

in vec2 textureCoords;

out vec4 out_colour;

uniform sampler2D colorTexture;
uniform vec2 u_resolution;
uniform vec2 texSize;
uniform vec2 blurDir;
uniform int kernel_radius;


float gaussianPdf(in float x, in float sigma) {
    return 0.39894 * exp( -0.5 * x * x/( sigma * sigma))/sigma;
}

void main(void){
    vec2 invSize = 1.0 / texSize;
    float fSigma = float(kernel_radius);
    float weightSum = gaussianPdf(0.0, fSigma);
    vec3 diffuseSum = texture(colorTexture, textureCoords).rgb * weightSum;
    for( int i = 1; i < kernel_radius; i ++ ) {
        float x = float(i);
        float w = gaussianPdf(x, fSigma);
        vec2 uvOffset = blurDir * invSize * x;
        vec3 sample1 = texture(colorTexture, textureCoords + uvOffset).rgb;
        vec3 sample2 = texture(colorTexture, textureCoords - uvOffset).rgb;
        diffuseSum += (sample1 + sample2) * w;
        weightSum += 2.0 * w;
    }
    out_colour = vec4(diffuseSum/weightSum, 1.0);
}
