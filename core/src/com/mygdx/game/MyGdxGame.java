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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

import jdk.nashorn.internal.runtime.Debug;
import sun.java2d.loops.DrawRect;
import sun.java2d.pipe.ShapeDrawPipe;

public class MyGdxGame extends ApplicationAdapter {

	//Declaracao de variaveis

	private SpriteBatch batch;
	private Texture[] naruto;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture moeda1;
	private	Texture moeda2;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	private Circle circuloMoeda1;

	private Circle circuloMoeda2;

	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private Random random;
	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private boolean passouCano = false;
	private int estadoJogo = 0;
	private float posicaoHorizontalPassaro = 0;

	private int posMin;

	private int posMax;

	private float posicaoVerticalMoeda1 = 0;

	private float posicaoHorizontalMoeda1 = 0;

	private float posicaoHorizontalMoeda2 = 0;

	private float posicaoVerticalMoeda2 =0;

	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	float posicaoMoeda = 0;



	@Override
	public void create () {

		inicializarTexturas();
		inicializaObjetos();

	}

	@Override
	//Metodo que renderiza o jogo na tela
	// Metodo que atualiza o jogo conforme ele roda
	public void render () {

		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT );
		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();

		shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		shapeRenderer.setColor(Color.RED);
		shapeRenderer.end();






	}


	private void inicializarTexturas(){

		//Instanciando texturas

		naruto = new Texture[6];
		naruto[0] = new Texture("NarutoRun/NarutoRun1.png");
		naruto[1] = new Texture("NarutoRun/NarutoRun2.png");
		naruto[2] = new Texture("NarutoRun/NarutoRun3.png");
		naruto[3] = new Texture("NarutoRun/NarutoRun4.png");
		naruto[4] = new Texture("NarutoRun/NarutoRun5.png");
		naruto[5] = new Texture("NarutoRun/NarutoRun6.png");

		fundo = new Texture("NarutoRun/Training Field.png");
		canoBaixo = new Texture("NarutoRun/TocoBaixo.png");
		canoTopo = new Texture("NarutoRun/TocoTopo.png");
		gameOver = new Texture("game_over.png");
		moeda1= new Texture("NarutoRun/Moeda1.png");
		moeda2= new Texture("NarutoRun/Moeda2.png");

	}

	private void inicializaObjetos(){




		//Instanciando Objetos, colisores, fontes, camera, viewport
		// e atrelando valores a variaveis
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();
		circuloMoeda1 = new Circle();
		circuloMoeda2 = new Circle();


		somVoando = Gdx.audio.newSound( Gdx.files.internal("som_asa.wav") );
		somColisao = Gdx.audio.newSound( Gdx.files.internal("som_batida.wav") );
		somPontuacao = Gdx.audio.newSound( Gdx.files.internal("som_pontos.wav") );

		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2,0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);





	}

	private void verificarEstadoJogo() {

		//Maquina de estado do jogo

		boolean toqueTela = Gdx.input.justTouched();
		if(estadoJogo == 0) {
			if(toqueTela){
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}

		}else if(estadoJogo ==1) {
			if(toqueTela){
				gravidade = -15;
				somVoando.play();
			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
			if( posicaoCanoHorizontal < -canoTopo.getWidth() ){
				posicaoCanoHorizontal = larguraDispositivo+100;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}
			if( posicaoInicialVerticalPassaro > 0 || toqueTela )
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;

			posicaoHorizontalMoeda1 -= Gdx.graphics.getDeltaTime() *200;
			if (posicaoHorizontalMoeda1 < -canoTopo.getWidth()){
				posicaoHorizontalMoeda1 = larguraDispositivo+100;
				posicaoVerticalMoeda1 = random.nextInt((int) alturaDispositivo);
			}
			posicaoHorizontalMoeda2 -= Gdx.graphics.getDeltaTime() *200;
			if (posicaoHorizontalMoeda2 < -canoTopo.getWidth()){
				posicaoHorizontalMoeda2 = larguraDispositivo+100;
				posicaoVerticalMoeda2 = random.nextInt((int) alturaDispositivo - 100);
			}


		}else if(estadoJogo ==2) {
			if( pontos > pontuacaoMaxima ) {
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

			if(toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;

			}
		}
	}


	private void detectarColisoes() {



		//demarcando colisores

		circuloPassaro.set(
				50 + posicaoHorizontalPassaro + naruto[0].getWidth() / 2,
				posicaoInicialVerticalPassaro + naruto[0].getHeight() / 2,
				naruto[0].getWidth() / 2
		);

		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);

		circuloMoeda1.set(
				posicaoHorizontalMoeda1, posicaoVerticalMoeda1,
				moeda1.getHeight() / 2

		);
		circuloMoeda2.set(
				posicaoHorizontalMoeda2, posicaoVerticalMoeda2,moeda2.getHeight()/2
		);


		//Checando se o passaro bateu no cano de cima ou de baixo
		//Tocando um som em caso afirmativo

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuMoeda1 = Intersector.overlaps(circuloPassaro,circuloMoeda1);
		boolean colidiuMoeda2 = Intersector.overlaps(circuloPassaro,circuloMoeda2);

		if (colidiuCanoCima || colidiuCanoBaixo) {
			if(estadoJogo == 1) {
				somColisao.play();
				estadoJogo = 2;
			}
		}
		if (colidiuMoeda1){

			pontos += 10;
			posicaoVerticalMoeda1 = alturaDispositivo*10;





		}
		if (colidiuMoeda2){
			pontos += 5;
			posicaoVerticalMoeda2 = alturaDispositivo*10;
		}

	}


	private void desenharTexturas() {
		//Desenhando as texturas nas dimencoes certas

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo,0,0,larguraDispositivo, alturaDispositivo);
		batch.draw(naruto[(int) variacao] ,
				50 + posicaoHorizontalPassaro,posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
		textoPontuacao.draw(batch, String.valueOf(pontos),larguraDispositivo / 2,
				alturaDispositivo - 110);
		batch.draw(moeda1, posicaoHorizontalMoeda1, posicaoVerticalMoeda1
				 );
		batch.draw(moeda2, posicaoHorizontalMoeda2 , posicaoVerticalMoeda2
		         );

		//Mostrando a tela de GameOver de acordo com o estado do jogo
		if (estadoJogo == 2) {
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth()/2,
					alturaDispositivo / 2);
			textoReiniciar.draw(batch,
					"Toque para reiniciar!", larguraDispositivo / 2 - 140,
					alturaDispositivo / 2 - gameOver.getHeight() / 2);
			textoMelhorPontuacao.draw(batch,
					"Seu record é: " + pontuacaoMaxima + "pontos",
					larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
		}
		batch.end();

	}


	public void validarPontos() {

		//verifica se o jogador passou pelos canos e registra os pontos

		if(posicaoCanoHorizontal < 50 - naruto[0].getWidth()) {
			if(!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}

		variacao += Gdx.graphics.getDeltaTime() * 10;

		if (variacao > 3)
			variacao = 0;

	}

	@Override
	//metodo que redimenciona o viewPort
	public void resize(int width, int height) {
		viewport.update(width, height);
	}



	@Override
	public void dispose () {

	}
}





