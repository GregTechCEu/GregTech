#version 120
// This content is under the MIT License. Created by Kali in 2015-02-06. https://www.shadertoy.com/view/XlX3Rj.

uniform vec2 u_resolution;
uniform vec2 u_mouse;
uniform float u_time;
uniform sampler2D texture;

#define time u_time*.02
#define width .005
float zoom = .18;

float shape=0.;
vec3 color=vec3(0.),randcol;

void formula(vec2 z, float c) {
    float minit=0.;
    float o,ot2,ot=ot2=1000.;
    for (int i=0; i<9; i++) {
        z=abs(z)/clamp(dot(z,z),.1,.5)-c;
        float l=length(z);
        o=min(max(abs(min(z.x,z.y)),-l+.25),abs(l-.25));
        ot=min(ot,o);
        ot2=min(l*.1,ot2);
        minit=max(minit,float(i)*(1.-abs(sign(ot-o))));
    }
    minit+=1.;
    float w=width*minit*2.;
    float circ=pow(max(0.,w-ot2)/w,6.);
    shape+=max(pow(max(0.,w-ot)/w,.25),circ);
    vec3 col=normalize(.1+texture2D(texture, vec2(minit*.1)).rgb);
    color+=col*(.4+mod(minit/9.-time*10.+ot2*2.,1.)*1.6);
    color+=vec3(1.,.7,.3)*circ*(10.-minit)*3.;
}


void mainImage( out vec4 fragColor, in vec2 fragCoord)
{
    vec2 pos = fragCoord.xy / u_resolution.xy*2.5 - .5;
    pos.x*=u_resolution.x/u_resolution.y;
    vec2 uv=pos;
    float sph = length(uv); sph = sqrt(1. - sph*sph)*1.5;
    uv=normalize(vec3(uv,sph)).xy;
    float a =time * 0.15 + mod(time,1.)*.5;
    vec2 luv=uv;
    float b=a*5.48535;
    uv*=mat2(cos(b),sin(b),-sin(b),cos(b));
    uv+=vec2(sin(u_mouse.x / u_resolution.x / 4.),cos(u_mouse.y  / u_resolution.y / 2.))*8.;
    uv*=zoom;
    float pix=.5/u_resolution.x*zoom/sph;
    float dof=max(1.,(10.-mod(time,1.)/.01));
    float c=1.5+mod(floor(time),6.)*.125;
    for (int aa=0; aa<36; aa++) {
        vec2 aauv=floor(vec2(float(aa)/6.,mod(float(aa),6.)));
        formula(uv+aauv*pix*dof,c);
    }
    shape/=36.; color/=36.;
    vec3 colo=mix(vec3(.15),color,shape);
    colo*=vec3(1.2,1.1,1.0)*(1.-length(pos))*min(1.,abs(.5-mod(time+.5,1.))*10.);
    fragColor = vec4(colo,1.0);
}

void main() {
    mainImage(gl_FragColor.rgba, vec2(gl_TexCoord[0].x * u_resolution.x, gl_TexCoord[0].y * u_resolution.y));
}
