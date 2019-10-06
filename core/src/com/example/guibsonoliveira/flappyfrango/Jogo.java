package com.example.guibsonoliveira.flappyfrango;

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
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter
{
	/*Texturas*/
	private SpriteBatch batch;
	private Texture fundo;
	private Texture frango;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	/*Formas para colisão*/
	private ShapeRenderer shapeRenderer;
	private Circle circuloFrango;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	/*Atributos de configuracao*/
    private float larguraDispositivo;
    private float alturaDispositivo;
    private float gravidade = 0;
    private float posicaoInicialFrangoY;
    private float posicaoInicialFrangoX;
    private float posicaoCanoX = 0;
    private float posicaoCanoY = 0;
    private float espacoEntreCanos;
    private Random random;
    private int pontos = 0;
    private int recorde = 0;
    private boolean passouCano = false;
    private float larguraFrango;
    private float alturaFrango;
    private int statusJogo = 0;

    /*Exibição de textos*/
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	/*Configuração dos sons*/
    Sound somVoando;
    Sound somColisao;
    Sound somPontuacao;

    /*Objeto salvar pontuação*/
    Preferences preferences;

    /*Objetos*/
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT= 1280;

	
	@Override
	public void create ()
	{
	    inicializarTexturas();
	    inicializarObjetos();
	}

	@Override
	public void render ()
	{
	    /*Limpar frames anteriores*/
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificarEstadoJogo();
		validarPontos();
		desenharObjetosTela();
		detectarColisoes();
	}

	@Override
	public void dispose () {
		//Gdx.app.log("dispose", "Descarte de conteudos");
	}

	private void verificarEstadoJogo()
	{
		/*
		 * 0 = Tela inicial jogo
		 * 1 = Começa jogo
		 * 2 = Colidiu
		 * */

		/*Retorna se foi tocado na tela = true*/
		boolean toqueTela = Gdx.input.justTouched();

		switch (statusJogo)
		{
			case 0:
				teleInicial(toqueTela);
				break;
			case 1:
				startGame(toqueTela);
				break;
			case 2:
				colidiu(toqueTela);
				break;
		}
	}


	private void teleInicial(boolean toqueTela)
	{
		/*Aplica evento de clique na tela*/
		if (toqueTela)
		{
            somVoando.play();
			gravidade = - 15;
			statusJogo = 1;
		}
	}

	private void startGame(boolean toqueTela)
	{
		/*Aplica evento de clique na tela*/
		if (toqueTela)
		{
            somVoando.play();
			gravidade = - 15;

		}

		/*Movimentar o cano*/
		posicaoCanoX -= Gdx.graphics.getDeltaTime() * 200;

		if (posicaoCanoX < - canoBaixo.getWidth())
		{
			posicaoCanoX = larguraDispositivo;
			posicaoCanoY = random.nextInt(800) - 400;
			passouCano = false;
		}

		/*Aplicar gravidade no passaro Ex: 400 - (-20) = 420*/
		if (posicaoInicialFrangoY >= 0 || toqueTela)
		{
			posicaoInicialFrangoY -= gravidade;
		}

		gravidade ++;
	}

	private void colidiu(boolean toqueTela)
	{
		/*Frango volta de ré*/
	    posicaoInicialFrangoX -= Gdx.graphics.getDeltaTime() * 600;

		/*Frango cai*/
		/*
		if (posicaoInicialFrangoY >= 0 || toqueTela)
			posicaoInicialFrangoY -= gravidade;

		gravidade ++;
		*/

		/*Teste se existe um novo Recorde*/
		if (pontos > recorde)
		{
			recorde = pontos;
			preferences.putInteger("recorde", recorde);
		}

		if (toqueTela)
		{
			statusJogo = 0;
			pontos = 0;
			gravidade = 0;
			posicaoInicialFrangoX = (float) (larguraDispositivo / 3.6);
			posicaoInicialFrangoY = alturaDispositivo / 2;
			posicaoCanoX = larguraDispositivo;
		}
	}

	private void detectarColisoes()
	{
		circuloFrango.set(
				posicaoInicialFrangoX + larguraFrango / 2,
				posicaoInicialFrangoY + alturaFrango / 2,
				larguraFrango/2
		);

		retanguloCanoCima.set(
				posicaoCanoX,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoY,
				canoTopo.getWidth(),
				canoTopo.getHeight()
		);

		retanguloCanoBaixo.set(
				posicaoCanoX,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoY,
				canoBaixo.getWidth(),
				canoBaixo.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloFrango, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloFrango, retanguloCanoBaixo);

		//Teste se o frango se sobrepos sobre o cano cima
		if (colidiuCanoCima || colidiuCanoBaixo || (posicaoInicialFrangoY <= 0))
		{
		    if (statusJogo == 1)
            {
                somColisao.play();
                statusJogo = 2;
            }

			Gdx.app.log("Log", "Frango colidiu com cano ");

		}

		/*
		//Utilizado para desenhar sobre as texturas
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.RED);

		shapeRenderer.circle(
		        posicaoInicialFrangoX + larguraFrango / 2,
                posicaoInicialFrangoY + alturaFrango / 2,
                larguraFrango/2
        );

        shapeRenderer.rect(
                posicaoCanoX,
                alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoY,
                canoTopo.getWidth(),
                canoTopo.getHeight()
        );

        shapeRenderer.rect(
        		posicaoCanoX,
                alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoY,
                canoBaixo.getWidth(),
                canoBaixo.getHeight()
        );

		shapeRenderer.end();
	*/
	}

	private void desenharObjetosTela()
	{
		/*Passa as configurações de exibição da camera*/
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		batch.draw(fundo,
                0, 0,
                larguraDispositivo,
                alturaDispositivo
        );

		batch.draw(frango,
                (float) posicaoInicialFrangoX,
                posicaoInicialFrangoY
        );

		batch.draw(canoBaixo,
                posicaoCanoX,
                alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoY
        );

		batch.draw(canoTopo, posicaoCanoX,
                alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoY
        );

		textoPontuacao.draw(batch,
                String.valueOf(pontos),
                larguraDispositivo / 2,
                (float) (alturaDispositivo / 1.08)
        );

		/*Ouve colisão e mostra a pontuação*/
        if (statusJogo == 2)
        {
            batch.draw(gameOver,
					larguraDispositivo / 2 - gameOver.getWidth() / 2,
					alturaDispositivo / 2
			);

            textoReiniciar.draw(batch,
					"Toque para reiniciar",
					larguraDispositivo/2 - gameOver.getWidth()/2,
					alturaDispositivo/2 - gameOver.getHeight()/2
			);

            textoMelhorPontuacao.draw(batch,
					"Seu record é: "+ recorde +" pontos",
					larguraDispositivo/2 - gameOver.getWidth()/2,
					alturaDispositivo/2 - gameOver.getHeight()
			);
        }

		batch.end();
	}

	private void validarPontos()
	{
		/*Passou da posicao do frango*/
		 if (posicaoCanoX < posicaoInicialFrangoX)
		 {
		 	if (!passouCano)
			{
				pontos ++;
				passouCano = true;
				somPontuacao.play();
			}
		 }
	}

	private void inicializarTexturas()
    {
        frango = new Texture("frango1.1.png");
        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");
        gameOver = new Texture("game_over.png");
    }

    private void inicializarObjetos()
    {
        batch = new SpriteBatch();
        random = new Random();

        /*Configura variaveis*/
        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        larguraFrango = frango.getWidth();
        alturaFrango = frango.getHeight();
        posicaoInicialFrangoY = alturaDispositivo / 2;
		posicaoInicialFrangoX = (float) (larguraDispositivo / 3.6);
        posicaoCanoX = larguraDispositivo;
		espacoEntreCanos = (float) (alturaFrango * 3.5);

		/*Configuração textos*/
		textoPontuacao = new BitmapFont();
		/*Cor texto*/
		textoPontuacao.setColor(Color.WHITE);
		/*Tamanho texto*/
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(3);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(3);

		/*Formas Geométricas para colisoes*/
		shapeRenderer = new ShapeRenderer();
		circuloFrango = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		/*Inicializa Sons*/
        somColisao = Gdx.audio.newSound(Gdx.files.internal("AiAi.mp3"));
        somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
        somVoando = Gdx.audio.newSound(Gdx.files.internal("Frango_clique1.mp3"));

        /*Configurar preferencias dos objetos*/
		preferences = Gdx.app.getPreferences("flappyFrango");
		recorde = preferences.getInteger("recorde", 0);

		/*Configuração câmera*/
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);

    }

	@Override
	public void resize(int width, int height)
	{
		viewport.update(width, height);
	}
}
