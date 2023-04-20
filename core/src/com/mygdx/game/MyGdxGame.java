package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class MyGdxGame extends ApplicationAdapter {
	SpriteBatch batch;
	Texture img;

	Texture[] texPassaro;
	Texture canoBaixo;
	Texture canoTopo;
	Texture backGround;
	Texture gameOver;

	ShapeRenderer shapeRenderer;
	Circle circle;
	Rectangle retanguloCanoCima;
	Rectangle retanguloCanoBaixo;

	float larguraDispositivo;
	float alturaDispositivo;
	float variacao = 0;
	float gravidade = 2;
	float posicaoInicial = 0;
	float posHorizontalCano;
	float posVerticalCano;
	float espacoEntreCanos;
	Random random;
	int pontos = 0;
	int maxPontos = 0;
	boolean passouCano = false;
	int estadoJogo = 0;
	float posicaoHorizontal;

	BitmapFont fontPontos;
	BitmapFont fontMaxPontos;
	BitmapFont fontReiniciar;

	Sound voando;
	Sound colisao;
	Sound somPontos;

	Preferences preferencias;

	OrthographicCamera camera;
	Viewport viewPort;
	final float VIRTUAL_WIDTH=720;
	final float VIRTUAL_HEIGHT=1280;





	@Override
	public void create () {

		inicializarTexturas();
		inicializarObjetos();
	}

	@Override
	public void render () {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_COLOR_BUFFER_BIT );
		verificarEstado();
		validarPontos();
		desenharTexturas();
		detectarColisoes();

	}

	@Override
	public void resize( int widht, int height){
		viewPort.update(widht,height);

	}
	
	@Override
	public void dispose () {

	}

	private void inicializarTexturas() {
		texPassaro = new Texture[3];
		texPassaro[0] = new Texture("passaro1.png");
		texPassaro[1] = new Texture("passaro2.png");
		texPassaro[2] = new Texture("passaro3.png");

		backGround = new Texture("fundo.png");

		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
	}
	private void inicializarObjetos(){
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicial = alturaDispositivo/2;
		posHorizontalCano = larguraDispositivo;
		espacoEntreCanos = 350;

		fontPontos = new BitmapFont();
		fontPontos.setColor(Color.WHITE);
		fontPontos.getData().setScale(10);

		fontReiniciar = new BitmapFont();
		fontReiniciar.setColor(Color.GREEN);
		fontReiniciar.getData().setScale(2);

		fontMaxPontos = new BitmapFont();
		fontMaxPontos.setColor(Color.RED);
		fontMaxPontos.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		circle = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		voando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		colisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontos = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		preferencias = Gdx.app.getPreferences("flappyBird");
		maxPontos = preferencias.getInteger("maxPontos",0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewPort = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT , camera);

	}

	private void verificarEstado(){
		boolean toqueTela = Gdx.input.justTouched();
		if(estadoJogo == 0){
			if(toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				voando.play();

			}
			else if (estadoJogo == 0) {
				if(toqueTela){
					gravidade=-15;
					voando.play();
				}
				posHorizontalCano -= Gdx.graphics.getDeltaTime() * 200;
				if(posHorizontalCano < -canoTopo.getWidth()){
					posHorizontalCano = larguraDispositivo;
					posVerticalCano = random.nextInt(400) - 200;
					passouCano = false;

				}
				if(posicaoInicial > 0 || toqueTela){
					posicaoInicial = posicaoInicial - gravidade;
					gravidade++;
				}
				else if (estadoJogo == 2){
					if(pontos > maxPontos){
						maxPontos = pontos;
						preferencias.putInteger("maxPontos", maxPontos);
						preferencias.flush();
					}
					posicaoHorizontal -= Gdx.graphics.getDeltaTime()*500;
				}
				if(toqueTela){
					estadoJogo = 0;
					pontos = 0;
					gravidade = 0;
					posicaoHorizontal = 0;
					posicaoInicial = alturaDispositivo/2;
					posHorizontalCano = larguraDispositivo;
				}

			}
		}
	}

	private void detectarColisoes(){
		circle.set(
				50+ posicaoHorizontal + texPassaro[0].getWidth()/2,
				posicaoInicial + texPassaro[0].getHeight()/2,
				texPassaro[0].getWidth()/2

		);
		retanguloCanoBaixo.set(
				posHorizontalCano,
				alturaDispositivo/2 - canoBaixo.getHeight()- espacoEntreCanos/2 + posVerticalCano,
				canoBaixo.getWidth(), canoTopo.getHeight()
		);
		retanguloCanoCima.set(
				posHorizontalCano, alturaDispositivo/2 + espacoEntreCanos/2 +posVerticalCano,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		boolean colidiuCanoCima = Intersector.overlaps(circle, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circle, retanguloCanoBaixo);

		if(colidiuCanoBaixo||colidiuCanoCima){
			colisao.play();
			estadoJogo = 2;
		}
	}
	private void desenharTexturas(){
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(backGround,0,0,larguraDispositivo,alturaDispositivo);
		batch.draw(texPassaro[(int) variacao],
				50 + posicaoHorizontal, posicaoInicial);

		batch.draw(canoBaixo, posHorizontalCano,
				alturaDispositivo/2 - canoBaixo.getHeight() - espacoEntreCanos/2 + posVerticalCano);
		batch.draw(canoTopo, posHorizontalCano,
				alturaDispositivo/2 + espacoEntreCanos/2 +posVerticalCano);
		fontPontos.draw(batch, String.valueOf(pontos), larguraDispositivo/2,
				alturaDispositivo/-110);

		if(estadoJogo == 2){
			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth(),
					alturaDispositivo/2);
			fontReiniciar.draw(batch,
					"TOQUE PARA REINICIAR !" , larguraDispositivo/2 - 140,
					alturaDispositivo/2 - gameOver.getHeight()/2);
			fontMaxPontos.draw(batch,
					" SEU SCORE É :"+maxPontos+" PONTOS",
					larguraDispositivo/2 -140, alturaDispositivo/2- gameOver.getHeight());

		}
		batch.end();
	}
	public void validarPontos(){
		if(posHorizontalCano < 50-texPassaro[0].getWidth()){
			if (!passouCano) {
				pontos++;
				passouCano = true;
				somPontos.play();

			}
		}
		variacao += Gdx.graphics.getDeltaTime() * 10;

		if(variacao > 3){
			variacao = 0;
		}

	}

}





