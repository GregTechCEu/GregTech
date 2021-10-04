#version 130

uniform sampler2D texture;
uniform vec2 u_resolution;

in vec2 textureCoords;

vec3 makeBloom(float lod, vec2 offset, vec2 bCoord){

    vec2 pixelSize = 1.0 / vec2(u_resolution.x, u_resolution.y);

    offset += pixelSize;

    float lodFactor = exp2(lod);

    vec3 bloom = vec3(0.0);
    vec2 scale = lodFactor * pixelSize;

    vec2 coord = (bCoord.xy-offset)*lodFactor;
    float totalWeight = 0.0;

    if (any(greaterThanEqual(abs(coord - 0.5), scale + 0.5)))
    return vec3(0.0);

    for (int i = -5; i < 5; i++) {
        for (int j = -5; j < 5; j++) {

            float wg = pow(1.0-length(vec2(i,j)) * 0.125,6.0);

            bloom = pow(texture2D(texture,vec2(i,j) * scale + lodFactor * pixelSize + coord, lod).rgb,vec3(2.2))*wg + bloom;
            totalWeight += wg;

        }
    }

    bloom /= totalWeight;

    return bloom;
}

void main() {
    vec3 blur = makeBloom(2.,vec2(0.0,0.0), textureCoords);
    blur += makeBloom(3.,vec2(0.3,0.0), textureCoords);
    blur += makeBloom(4.,vec2(0.0,0.3), textureCoords);
    blur += makeBloom(5.,vec2(0.1,0.3), textureCoords);
    blur += makeBloom(6.,vec2(0.2,0.3), textureCoords);
    gl_FragColor = vec4(pow(blur, vec3(1.0 / 2.2)),1.0);
}

