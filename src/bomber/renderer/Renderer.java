package bomber.renderer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;

import bomber.game.Bomb;
import bomber.game.GameState;
import bomber.game.Player;
import bomber.renderer.shaders.ShaderProgram;
import bomber.renderer.utils.FileHandler;
import bomber.renderer.utils.Transformation;

public class Renderer {

	private ShaderProgram shaderConstructor;
	private final Transformation transformation;

	public Renderer() {

		transformation = new Transformation();
	} // END OF CONSTRUCTOR

	public void init(Screen screen) throws Exception {

		shaderConstructor = new ShaderProgram();
		shaderConstructor.createVertexShader(FileHandler.loadResource("res/vertex.vs"));
		shaderConstructor.createFragmentShader(FileHandler.loadResource("res/fragment.fs"));
		shaderConstructor.link();

		shaderConstructor.createUniform("projection");
		shaderConstructor.createUniform("model");

		screen.setClearColour(0f, 0f, 0f, 0f);
	} // END OF init METHOD

	// Takes a state to render
	public void render(Screen screen, GameState state) {

		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

		// Resize the screen if it needs to be resized
		if (screen.isResized()) {

			screen.setViewport(0, 0, screen.getWidth(), screen.getHeight());
			screen.setResized(false);
		}

		// Bind the shader
		shaderConstructor.bind();

		// Set the uniform
		Matrix4f projectionMatrix = transformation.getOrthographicProjection(0f, screen.getWidth(), screen.getHeight(), 0f, -1f, 1f);
		shaderConstructor.setUniform("projection", projectionMatrix);

		// Render each entity of the state
		
		/*
		for (GameEntity gameEntity : gameEntities) {

			Matrix4f modelMatrix = transformation.getModelMatrix(gameEntity.getPosition(), gameEntity.getRotation(),
					gameEntity.getScale());
			
			shaderConstructor.setUniform("model", modelMatrix);

			gameEntity.getMesh().render();
		}*/
		
		
		for (Player player : state.getPlayers()) {
			
			player.getMesh().render();
		}
		
		for (Bomb bomb : state.getBombs()) {
			
			bomb.getMesh().render();
		}

		// Unbind the shader
		shaderConstructor.unbind();

	} // END OF render METHOD
	
	public void dispose() {

		if (shaderConstructor != null) {

			shaderConstructor.dispose();
		}

	} // END OF dispose METHOD
} // END OF Renderer CLASS
