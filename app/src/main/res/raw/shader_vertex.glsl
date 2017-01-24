#version 300 es

in vec3 in_position;
in vec3 in_normal;
in vec2 in_texcoord;

//in vec3 in_color;
//in vec3 in_position;

uniform mat4 model_matrix, view_matrix, projection_matrix;

out vec3 world_pos;
out vec3 world_normal;
out vec2 texcoord;

void main(){

	world_pos = vec3(model_matrix * vec4(in_position, 1)); // pozitia in coordonate word
	world_normal = normalize(mat3(model_matrix) * in_normal);
	texcoord = in_texcoord;

	gl_Position = projection_matrix*view_matrix*model_matrix*vec4(in_position,1);
}