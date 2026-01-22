#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in vec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;

out vec2 vUv;
out vec4 vColor;
out vec3 vNrm;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vUv = UV0;
    vColor = Color * ColorModulator;

    mat3 nMat = mat3(ModelViewMat);
    vNrm = normalize(nMat * Normal);
}
