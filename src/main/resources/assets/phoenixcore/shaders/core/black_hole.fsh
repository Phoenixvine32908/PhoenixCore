#version 150

uniform sampler2D Sampler0;
uniform vec2 ScreenSize;
uniform vec2 BlackHolePos;
uniform float GameTime;

uniform float BlackHoleRadius;        // ~0.04..0.10 in screen UV
uniform float DistortionStrength;     // ~0.8..2.0

out vec4 fragColor;

float saturate(float x) { return clamp(x, 0.0, 1.0); }

void main() {
    vec2 uv = gl_FragCoord.xy / ScreenSize;

    // aspect-corrected delta
    float aspect = ScreenSize.x / ScreenSize.y;
    vec2 d = uv - BlackHolePos;
    d.x *= aspect;

    float r = length(d);
    float rs = BlackHoleRadius;

    // inside event horizon
    if (r < rs) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec2 dir = d / max(r, 1e-5);

    // simple lensing curve (tweakable, stable)
    float x = max(r - rs, 1e-4);
    float bend = (rs * rs) / x;
    bend *= 0.015 * DistortionStrength;

    // warp sample UV (undo aspect on x when going back to screen UV)
    vec2 warp = dir * bend;
    warp.x /= aspect;

    vec2 sampleUV = uv - warp;

    // chromatic aberration
    float ca = 0.0015 * DistortionStrength;
    vec3 col;
    col.r = texture(Sampler0, sampleUV + vec2(ca, 0.0)).r;
    col.g = texture(Sampler0, sampleUV).g;
    col.b = texture(Sampler0, sampleUV - vec2(ca, 0.0)).b;

    // photon ring (thin bright ring near rs)
    float ring = 1.0 - saturate(abs(r - (rs * 1.35)) / (rs * 0.12));
    ring = pow(ring, 3.0);

    // subtle animated shimmer
    float t = GameTime * 0.35;
    float shimmer = 0.5 + 0.5 * sin(t + r * 120.0);
    ring *= mix(0.85, 1.15, shimmer);

    vec3 ringColor = vec3(1.2, 0.9, 0.6) * ring * 0.75;

    // gentle darkening toward the hole
    float vignette = saturate((r - rs) / (rs * 5.0));
    col *= mix(0.25, 1.0, vignette);

    fragColor = vec4(col + ringColor, 1.0);
}
