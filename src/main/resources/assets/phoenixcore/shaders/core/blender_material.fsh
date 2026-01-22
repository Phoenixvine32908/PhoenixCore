#version 150

uniform sampler2D Sampler0; // Albedo
uniform sampler2D Sampler1; // Normal (_n)
uniform sampler2D Sampler2; // Params (_s): R=metallic, G=roughness

in vec2 vUv;
in vec4 vColor;
in vec3 vNrm;

out vec4 fragColor;

float saturate(float x) { return clamp(x, 0.0, 1.0); }

void main() {
    vec4 albedo = texture(Sampler0, vUv) * vColor;
    if (albedo.a < 0.1) discard;

    vec3 nMap = texture(Sampler1, vUv).rgb * 2.0 - 1.0;
    vec3 N = normalize(vNrm + nMap * 0.75);

    vec4 p = texture(Sampler2, vUv);
    float metallic  = saturate(p.r);
    float roughness = saturate(p.g);

    vec3 L = normalize(vec3(0.4, 1.0, 0.2));
    vec3 V = normalize(vec3(0.0, 0.0, 1.0));
    vec3 H = normalize(L + V);

    float NoL = max(dot(N, L), 0.0);
    float NoH = max(dot(N, H), 0.0);

    float ambient = 0.25;
    vec3 diffuse = albedo.rgb * (ambient + NoL * (1.0 - ambient));

    float gloss = 1.0 - roughness;
    float specPow = mix(8.0, 128.0, gloss);
    float spec = pow(NoH, specPow);

    float f0 = mix(0.04, 1.0, metallic);
    float fres = f0 + (1.0 - f0) * pow(1.0 - max(dot(N, V), 0.0), 5.0);

    vec3 specular = vec3(spec) * fres * (0.35 + 0.65 * metallic);

    fragColor = vec4(diffuse + specular, albedo.a);
}
