#version 150

uniform sampler2D Sampler0; // The screen buffer
uniform vec2 ScreenSize;
uniform vec2 BlackHolePos;
uniform float GameTime;

in vec2 texCoord0;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize;
    float aspect = ScreenSize.x / ScreenSize.y;

    // Vector to center
    vec2 delta = uv - BlackHolePos;
    delta.x *= aspect;
    float dist = length(delta);

    float rs = 0.04; // Event Horizon Radius

    if (dist < rs) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0); // The Void
    } else {
        // GRAVITATIONAL LENSING MATH
        // Distortion is 1/r. This creates the 'Einstein Ring'.
        float distortion = (rs * rs) / (dist - rs + 0.01);
        vec2 warpedUV = uv - (normalize(delta) * distortion);

        // SCREEN SPACE REFLECTION (SSR)
        // We sample a reflected point to simulate light bending
        // from the "back" of the accretion disk to the front.
        vec2 reflectUV = uv + (normalize(delta) * (distortion * 0.5));

        vec4 scene = texture(Sampler0, warpedUV);
        vec4 reflection = texture(Sampler0, reflectUV) * 0.3; // Fake reflection

        // Add a subtle blue/orange chromatic aberration at the edge
        float r = texture(Sampler0, warpedUV + vec2(0.001, 0.0)).r;
        float g = scene.g;
        float b = texture(Sampler0, warpedUV - vec2(0.001, 0.0)).b;

        fragColor = vec4(vec3(r, g, b) + reflection.rgb, 1.0);
    }
}