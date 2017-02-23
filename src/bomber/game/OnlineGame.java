package bomber.game;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import bomber.AI.GameAI;
import bomber.UI.UserInterface;
import bomber.audio.AudioManager;
import bomber.networking.ClientThread;
import bomber.physics.PhysicsEngine;
import bomber.renderer.Graphics;
import bomber.renderer.Renderer;
import bomber.renderer.Screen;
import bomber.renderer.interfaces.GameInterface;
import bomber.renderer.shaders.Mesh;

public class OnlineGame implements GameInterface {

	private String playerName;
	private Map map;
	private HashMap<Response, Integer> controlScheme;
	private Screen screen;
	private GameState gameState;
	private KeyboardState keyState;
	private Graphics graphics;
	private Renderer renderer;
	private boolean bombPressed;
	private KeyboardInput input;
	private Player player;
	private AudioManager audio;
	private UserInterface ui;
	private int aiNum;
	private ClientThread client;

	public OnlineGame(UserInterface ui, ClientThread client, GameState gameState, Map map, String playerName, HashMap<Response, Integer> controls) {

		this.ui = ui;
		this.gameState = gameState;
		this.client = client;
		this.map = map;
		this.playerName = playerName;
		this.controlScheme = controls;
		this.bombPressed = false;
		this.input = new KeyboardInput();
		this.renderer = new Renderer();
		audio = new AudioManager();
		audio.playMusic();

		try {
			
			int width = this.map.getPixelMap().length;
			int height = this.map.getPixelMap()[0].length;
			this.graphics = new Graphics("Bomb Blitz", width, height, true, this);
			this.graphics.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void init(Screen screen) {
		try {
			System.out.println("Giving screen to renderer");
			this.renderer.init(screen);
			float[] colours = new float[] { 0.1f, 0.3f, 0.5f, 0f, 0.1f, 0.3f, 0.5f, 0f, 0.1f, 0.3f, 0.5f, 0f };

			Thread.sleep(500);
			this.gameState = this.client.getGameState();
			for(Player player : this.gameState.getPlayers()){
				
				player.addMesh(new Mesh(32, 32, colours));
			}
			this.keyState = this.gameState.getPlayers().get(0).getKeyState();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.ui.hide();
	}

	@Override
	public void update(float interval) {

		this.gameState = this.client.getGameState();
		float[] colours = new float[] { 0.1f, 0.3f, 0.5f, 0f, 0.1f, 0.3f, 0.5f, 0f, 0.1f, 0.3f, 0.5f, 0f };
		for(Player player : this.gameState.getPlayers()){
			
			player.addMesh(new Mesh(32, 32, colours));
		}
		this.keyState = this.gameState.getPlayers().get(0).getKeyState();

		this.keyState.setBomb(false);
		this.keyState.setMovement(Movement.NONE);
		audio.playEventList(gameState.getAudioEvents());
	}

	@Override
	public void render(Screen screen) {

		this.renderer.render(screen, this.gameState);
	}

	@Override
	public void input(Screen screen) {

		this.bombPressed = this.input.update(screen, this.keyState, this.controlScheme, this.bombPressed);
		try {
			this.client.sendMove(keyState);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void dispose() {

		this.ui.show();
		System.out.println("RETURNED TO MENU");
		renderer.dispose();
		this.graphics.getScreen().close();
		audio.stopAudio();
		
	}
}