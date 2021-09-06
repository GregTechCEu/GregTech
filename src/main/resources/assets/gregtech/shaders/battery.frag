#version 120

//#extension GL_OES_standard_derivatives : enable
uniform float u_time;
uniform vec2 u_resolution;
uniform vec3 c_ring;
uniform vec3 c_sector;
uniform vec3 c_water;
uniform float progress;

float aa(float x, float s) {
    float a = s * fwidth(x);
    return smoothstep(a, -a, x);
}


vec3 water(vec2 uv, float progress) {
    float ao = sin(uv.x * 40. + u_time) * .01 - .02;
    float a = aa(uv.y-(progress + ao), 1.);
    float bo = sin(uv.x * 24. + u_time + 3.14) * .0255;
    float b = aa(uv.y-(progress + bo), 1.);
    vec3 ca = vec3(c_water.x, c_water.y + uv.y * .8, c_water.z);
    return ca * (a * .3 + b * .2);
}

void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    vec2 uv = fragCoord.xy / u_resolution.xy;
    vec2 s = (2. * fragCoord.xy - u_resolution.xy) / u_resolution.y;
    float m = dot(s, s) - .8;
    float pa = mod(atan(s.y, s.x) + u_time * 1.7, 6.28) - 3.14;
    pa = smoothstep(0., .4, pa * pa);
    float ic = aa(m, 1.);
    vec3 w = ic * water(fragCoord.xy/u_resolution.xy, progress);
    float oc = aa(-m+.11, 1.) * aa(m-.13, 1.);
    vec3 occ = oc * c_ring + oc * (1. - pa) * c_sector * 2.;
    float a = 0.;
    if (length(occ) > 0.) {
        a = 1.;
    } else if (length(w) > 0.1) {
        a = 0.9;
    }
    fragColor = vec4(w.rgb + occ.rgb, a);
}

void main() {
    mainImage(gl_FragColor.rgba, vec2(gl_TexCoord[0].x * u_resolution.x, gl_TexCoord[0].y * u_resolution.y));
}
