#version 150

uniform sampler2D Sampler0;     // scene color (copied to scratch)
uniform vec2  ScreenSize;       // window size in pixels
uniform vec2  BlackHolePos;     // center in screen UV (0..1)
uniform float BlackHoleRadius;  // radius in screen UV
uniform float DistortionStrength;
uniform float GameTime;

in vec2 texCoord0;
out vec4 fragColor;

float saturate(float x) { return clamp(x, 0.0, 1.0); }

void main() {
    vec2 uv = texCoord0;

    float aspect = ScreenSize.x / ScreenSize.y;

    vec2 delta = uv - BlackHolePos;
    delta.x *= aspect;

    float dist = length(delta);

    float rs = max(0.0005, BlackHoleRadius); // event horizon radius in UV

    // Outside influence radius: just show the scene (no weird background)
    float influence = rs * 6.0;
    if (dist >= influence) {
        fragColor = texture(Sampler0, uv);
        return;
    }

    // Event horizon: pure black
    if (dist <= rs) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    // Smooth falloff (1 near center, 0 at influence edge)
    float falloff = smoothstep(influence, rs, dist);

    // Lensing distortion ~ 1/r, softened to avoid singularity and shimmering
    vec2 dir = normalize(delta);
    float denom = max(0.0025, dist - rs);
    float distortion = (rs * rs) / denom;

    distortion *= (0.25 + 0.75 * falloff) * DistortionStrength;

    // Warp UV (undo aspect correction for sampling)
    vec2 warp = dir * distortion;
    warp.x /= aspect;

    vec2 warpedUV  = uv - warp;
    vec2 reflectUV = uv + warp * 0.35;

    // Clamp to avoid sampling outside the texture (edge/background artifacts)
    warpedUV  = clamp(warpedUV,  vec2(0.001), vec2(0.999));
    reflectUV = clamp(reflectUV, vec2(0.001), vec2(0.999));

    vec4 scene = texture(Sampler0, warpedUV);

    // subtle fake back-light / ring hint
    vec4 reflection = texture(Sampler0, reflectUV) * 0.25 * falloff;

    // tiny chromatic aberration near the edge
    float ca = 0.00075 * falloff;
    float r = texture(Sampler0, clamp(warpedUV + vec2( ca, 0.0), vec2(0.001), vec2(0.999))).r;
    float g = scene.g;
    float b = texture(Sampler0, clamp(warpedUV + vec2(-ca, 0.0), vec2(0.001), vec2(0.999))).b;

    vec3 color = vec3(r, g, b) + reflection.rgb;

    fragColor = vec4(color, 1.0);
}
