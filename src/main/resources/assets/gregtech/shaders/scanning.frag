#version 120

uniform float u_time;
uniform float radius;

uniform sampler2D depthTexture;

uniform vec2 u_resolution;
uniform float u_zFar; // mc.gameSettings.renderDistanceChunks * 16 * MathHelper.SQRT_2
uniform float u_FOV;

varying vec2 textureCoords;

const float zNear = 0.05; // default of entity view
const float width = 10;

void main() {
    vec4 color = vec4(0, 0, 0, 0);
    float depth = texture2D(depthTexture, textureCoords).r;
    float aspect = u_resolution.x / u_resolution.y;

    float linearDepth = 2*zNear*u_zFar / (u_zFar + zNear - (2.0*depth - 1.0)*(u_zFar - zNear));
//    vec3 worldPos = camPos + vec3(0, 1.5, 0.) + vec3(1., (2.*textureCoords - 1.) * vec2(aspect,1.) * tan(radians(_FOV / 2.))) * linearDepth;
    float dist = length(vec3(1., (2.*textureCoords - 1.) * vec2(aspect,1.) * tan(radians(u_FOV / 2.))) * linearDepth);

  if (dist < radius && dist > radius - width && depth < 1) {
      vec2  px = 10.0*(-u_resolution.xy + 5.0*gl_FragCoord.xy) / u_resolution.y;
      float id = 0.3 * cos(u_time * 5. + sin(dot(floor(px + 0.5),vec2(0.95,0.6)))*10000.);
      vec3  co = vec3(0.005,2.000,1.108);
      vec2  pa = smoothstep( 0.0, 0.2, id*(0.5 + 0.5*cos(6.2831*px)) );
      color = vec4( co*pa.x*pa.y, smoothstep(radius-width,radius,dist));
  }

  gl_FragColor = color;
}
