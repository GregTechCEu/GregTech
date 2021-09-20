#version 120

uniform vec2 u_resolution;
uniform float u_time;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
    vec2 gv = fragCoord/u_resolution.xy;
    vec2 uv = (fragCoord-0.5*u_resolution.xy)/u_resolution.y * 2.;
    vec3 col = vec3(0);

    float t = mod(u_time, 2.) / 2.;
    float width = 0.1;
    float radius = cos(u_time)*0.2 + 0.6; //Using tan to create a repeating scan
    float c = length(uv);

    //Creating the circle based upon radius and width
    col += smoothstep(radius+width, radius+width-0.001, c);
    col -= smoothstep(radius, radius-0.02, c);

    //Creating a linear gradient from inside to out of the ring
    col *= vec3(0.5,0.800,0.6)*1.4*((c-radius+0.03)/(width-0.01));

    //Adding White ouline on the circle
    col += smoothstep(radius+width, radius+width-0.01, c);
    col -= smoothstep(radius+width-0.015, radius+width-0.02, c);
    col += (sin(c*275.)*0.2)*col; //Holographic type waves

    //col = vec3(c);
    fragColor = vec4(col, 1.);
}

void main() {
    mainImage(gl_FragColor.rgba, vec2(gl_TexCoord[0].x * u_resolution.x, gl_TexCoord[0].y * u_resolution.y));
}
