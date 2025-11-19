package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont; // Import necessário
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle; // Import necessário
import com.badlogic.gdx.math.Vector3; // Import necessário
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    SpriteBatch spriteBatch;
    FitViewport viewport;

    Fundo fundo;
    Personagem personagem;

    // Inimigos
    Array<Fantasma> fantasmas;
    float tempoGerarFantasma = 0f;
    float intervaloGeracaoFantasma = 3f;
    float velocidadeFantasma = 2.5f;

    Array<EsqueletoVoador> esqueletosVoadores;
    float tempoGerarEsqueleto = 0f;
    float intervaloGeracaoEsqueleto = 6f;
    float velocidadeEsqueleto = 3.0f;
    final float ALTURA_VOO = 3.0f;

    boolean personagemAtivo = true;

    // Controle de brilho
    private Texture fundoBrilho;
    private float brilho = 1f;
    private final float velocidadeEscurecer = 0.3f;
    private final float velocidadeClarear = 0.5f;

    // Variáveis para Exibição da Fase
    private Texture[] fases;
    private boolean exibirFase = false;
    private float tempoExibicaoFase = 0f;
    private final float duracaoExibicao = 3f;
    private int faseAtualParaExibir = 0;

    // VARIÁVEIS DO MENU DE PAUSA
    private boolean jogoPausado = false;
    private Rectangle btnRetomar;
    private Rectangle btnReiniciar;
    private Rectangle btnSair;
    private Texture texturaBotao;

    // Texturas dos Ícones
    private Texture iconResume;
    private Texture iconRestart;
    private Texture iconExit;

    // VARIÁVEIS DE PONTUAÇÃO
    private int score = 0;
    private BitmapFont fontScore;

    // --- NOVO: Variável da tela de Game Over e música ---
    private Texture texturaGameOver;
    private com.badlogic.gdx.audio.Music backgroundMusic;
    // ------------------------------------------


    @Override
    public void create() {
        if (spriteBatch == null) spriteBatch = new SpriteBatch();
        if (viewport == null) viewport = new FitViewport(8, 5);

        fundo = new Fundo();
        personagem = new Personagem();
        personagem.centralizar(viewport);

        fantasmas = new Array<>();
        esqueletosVoadores = new Array<>();

        tempoGerarFantasma = 0f;
        tempoGerarEsqueleto = 1f;
        personagemAtivo = true;
        jogoPausado = false;

        // Cria textura preta para o controle de brilho
        if (fundoBrilho == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            fundoBrilho = new Texture(pixmap);
            pixmap.dispose();
        }

        // Carregamento das Texturas das Fases
        fases = new Texture[]{
            new Texture("fase1.png"),
            new Texture("fase2.png"),
            new Texture("fase3.png")
        };

        // Inicia exibindo a Fase 1
        exibirFase = true;
        tempoExibicaoFase = 0f;
        faseAtualParaExibir = 0;

        // CONFIGURAÇÃO DA FONTE DE PONTUAÇÃO (Com correção para o '1' e escala aumentada)
        fontScore = new BitmapFont();
        fontScore.setColor(Color.WHITE);
        fontScore.getData().setScale(0.10f);

        // --- CONFIGURAÇÃO DE TEXTURAS DE MENU E GAME OVER ---

        // NOVO: Carregamento da Textura de Game Over
        texturaGameOver = new Texture("game_over.png");

        // Carregamento dos Ícones
        iconResume = new Texture("icon_resume.png");
        iconRestart = new Texture("icon_restart.png");
        iconExit = new Texture("icon_exit.png");

        // Configuração Visual dos Botões
        Pixmap pixmapBotao = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmapBotao.setColor(Color.BLUE);
        pixmapBotao.fill();
        texturaBotao = new Texture(pixmapBotao);
        pixmapBotao.dispose();

        // Posição e Tamanho dos Botões
        float btnLargura = 3.0f;
        float btnAltura = 0.5f;
        float centroX = (viewport.getWorldWidth() - btnLargura) / 2;
        float centroY = viewport.getWorldHeight() / 2;
        float espacamento = 0.8f;

        btnRetomar = new Rectangle(centroX, centroY + espacamento, btnLargura, btnAltura);
        btnReiniciar = new Rectangle(centroX, centroY, btnLargura, btnAltura);
        btnSair = new Rectangle(centroX, centroY - espacamento, btnLargura, btnAltura);

        // --- NOVO: Carregamento e Configuração da Música ---
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("background_music.mp3"));

        // 1. Define o volume inicial (0.0 a 1.0)
        backgroundMusic.setVolume(0.5f);

        // 2. Define para tocar em loop infinito
        backgroundMusic.setLooping(true);

        // 3. Inicia a música
        backgroundMusic.play();
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // LÓGICA DE PAUSA (Tecla 'P')
        if (Gdx.input.isKeyJustPressed(Input.Keys.P) && personagemAtivo) {
            jogoPausado = !jogoPausado;

            // --- NOVO: Controla a Música na Pausa ---
            if (jogoPausado) {
                backgroundMusic.pause();
            } else {
                backgroundMusic.play();
            }
        }

        // Reiniciar ao apertar R (se não estiver pausado)
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && !jogoPausado) {
            reiniciarJogo();
            return;
        }

        // Reiniciar após Game Over com a tecla ESPAÇO
        if (!jogoPausado && !personagemAtivo && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            reiniciarJogo();
            return;
        }

        // UPDATE LÓGICA DE ATUALIZAÇÃO (SÓ RODA SE NÃO ESTIVER PAUSADO) ---
        if (!jogoPausado) {

            if (personagemAtivo) {
                input(delta);
                personagem.update(delta, viewport);
            }

            fundo.atualizar(delta);

            // Lógica de Exibição de Fase
            if (fundo.getTrocouFundo()) {
                exibirFase = true;
                tempoExibicaoFase = 0f;
                faseAtualParaExibir = fundo.getIndiceAtual();
            }

            if (exibirFase) {
                tempoExibicaoFase += delta;
                if (tempoExibicaoFase >= duracaoExibicao) {
                    exibirFase = false;
                }
            }

            // Atualiza o brilho
            if (Gdx.input.isKeyPressed(Input.Keys.S)) {
                brilho += velocidadeClarear * delta;
            } else {
                brilho -= velocidadeEscurecer * delta;
            }
            brilho = MathUtils.clamp(brilho, 0f, 1f);

            // Aumenta velocidade conforme troca o fundo
            switch (fundo.getIndiceAtual()) {
                case 0:
                    velocidadeFantasma = 5f;
                    velocidadeEsqueleto = 5f;
                    intervaloGeracaoFantasma = 3f;
                    intervaloGeracaoEsqueleto = 6f;
                    break;
                case 1:
                    velocidadeFantasma = 7f;
                    velocidadeEsqueleto = 7f;
                    intervaloGeracaoFantasma = 2.5f;
                    intervaloGeracaoEsqueleto = 5f;
                    break;
                case 2:
                    velocidadeFantasma = 9f;
                    velocidadeEsqueleto = 9f;
                    intervaloGeracaoFantasma = 2f;
                    intervaloGeracaoEsqueleto = 4f;
                    break;
            }

            // Geração e Atualização de Fantasmas (Inimigos de Chão)
            tempoGerarFantasma += delta;
            if (tempoGerarFantasma >= intervaloGeracaoFantasma) {
                tempoGerarFantasma = 0f;
                float alturaChao = 0f;
                fantasmas.add(new Fantasma(viewport, alturaChao, velocidadeFantasma));
            }

            // Atualiza e verifica colisão dos Fantasmas
            for (int i = fantasmas.size - 1; i >= 0; i--) {
                Fantasma f = fantasmas.get(i);
                f.update(delta);

                if (personagemAtivo && f.getHitbox().overlaps(personagem.getHitbox())) {
                    personagemAtivo = false;
                    backgroundMusic.stop(); // <--- NOVO: Para a música no Game Over
                }

                if (personagemAtivo && f.getHitbox().overlaps(personagem.getHitbox())) {
                    personagemAtivo = false;
                }

                if (f.saiuDaTela()) {
                    // Incremento de score (sem limite)
                    if (personagemAtivo) {
                        score++;
                    }
                    f.dispose();
                    fantasmas.removeIndex(i);
                }
            }

            // Geração e Atualização de Esqueletos Voadores (Inimigos Aéreos)
            tempoGerarEsqueleto += delta;
            if (tempoGerarEsqueleto >= intervaloGeracaoEsqueleto) {
                tempoGerarEsqueleto = 0f;
                esqueletosVoadores.add(new EsqueletoVoador(viewport, ALTURA_VOO, velocidadeEsqueleto));
            }

            // Atualiza e verifica colisão dos Esqueletos Voadores
            for (int i = esqueletosVoadores.size - 1; i >= 0; i--) {
                EsqueletoVoador e = esqueletosVoadores.get(i);
                e.update(delta);

                if (personagemAtivo && e.getHitbox().overlaps(personagem.getHitbox())) {
                    personagemAtivo = false;
                }

                if (e.saiuDaTela()) {
                    e.dispose();
                    esqueletosVoadores.removeIndex(i);
                }
            }
        }
        // FIM DA LÓGICA DE ATUALIZAÇÃO
        // ----------------------------------------------------------------------


        // --- LÓGICA DE CLIQUE (SEMPRE RODA PARA PODERMOS CLICAR NO MENU) ---
        if (jogoPausado && Gdx.input.justTouched()) {
            Vector3 touchPoint = viewport.unproject(
                new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0)
            );

            if (btnRetomar.contains(touchPoint.x, touchPoint.y)) {
                retomarJogo();
            } else if (btnReiniciar.contains(touchPoint.x, touchPoint.y)) {
                reiniciarJogo();
            } else if (btnSair.contains(touchPoint.x, touchPoint.y)) {
                sairDoJogo();
            }
        }
        // FIM DA LÓGICA DE CLIQUE
        // --------------------------------------------------------------------------------------


        // --- RENDERIZAÇÃO ---
        ScreenUtils.clear(Color.WHITE);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        fundo.render(spriteBatch, viewport);
        if (personagemAtivo) personagem.render(spriteBatch);

        for (Fantasma f : fantasmas) f.render(spriteBatch);
        for (EsqueletoVoador e : esqueletosVoadores) e.render(spriteBatch);

        // Camada escura (controle de brilho)
        spriteBatch.setColor(0, 0, 0, 1f - brilho);
        spriteBatch.draw(fundoBrilho, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        spriteBatch.setColor(Color.WHITE);

        // Desenha a Fase
        if (exibirFase) {
            Texture faseTextura = fases[faseAtualParaExibir];
            float faseLargura = 6f;
            float faseAltura = 2f;
            float faseX = (viewport.getWorldWidth() - faseLargura) / 2;
            float faseY = (viewport.getWorldHeight() - faseAltura) / 2;
            spriteBatch.draw(faseTextura, faseX, faseY, faseLargura, faseAltura);
        }

        // --- EXIBIÇÃO DA PONTUAÇÃO ---
        float scoreX = 0.2f;
        float scoreY = viewport.getWorldHeight() - 0.2f;
        if (personagemAtivo) {
            fontScore.draw(spriteBatch, "" + score, scoreX, scoreY);
        }
        // -----------------------------------

        // --- NOVO: LÓGICA DE TELA DE GAME OVER ---
        if (!personagemAtivo) {
            // 1. Fundo escuro para destacar a mensagem
            spriteBatch.setColor(0, 0, 0, 0.7f);
            spriteBatch.draw(fundoBrilho, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

            // 2. Desenha a imagem de Game Over centralizada
            spriteBatch.setColor(Color.WHITE);

            float goLargura = 6f; // Ajuste conforme o tamanho do seu 'game_over.png'
            float goAltura = 3f;  // Ajuste conforme o tamanho do seu 'game_over.png'

            float goX = (viewport.getWorldWidth() - goLargura) / 2;
            float goY = (viewport.getWorldHeight() - goAltura) / 2;

            spriteBatch.draw(texturaGameOver, goX, goY, goLargura, goAltura);

            // Instrução de reinício
            fontScore.getData().setScale(0.06f);
            fontScore.draw(spriteBatch, "", (viewport.getWorldWidth() - 5f) / 2, goY - 0.5f);
            fontScore.getData().setScale(0.10f); // Volta a escala original
        }
        // -------------------------------------------


        // --- RENDERIZAÇÃO DO MENU DE PAUSA (COM ÍCONES) ---
        if (jogoPausado) {
            // Fundo escuro semitransparente para o menu (acima de tudo)
            spriteBatch.setColor(0, 0, 0, 0.7f);
            spriteBatch.draw(fundoBrilho, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

            // Define cor Azul-Turquesa Suave (0.4f, 0.8f, 0.8f)
            spriteBatch.setColor(0.4f, 0.8f, 0.8f, 1f);

            // Desenha os Retângulos
            spriteBatch.draw(texturaBotao, btnRetomar.x, btnRetomar.y, btnRetomar.width, btnRetomar.height);
            spriteBatch.draw(texturaBotao, btnReiniciar.x, btnReiniciar.y, btnReiniciar.width, btnReiniciar.height);
            spriteBatch.draw(texturaBotao, btnSair.x, btnSair.y, btnSair.width, btnSair.height);

            spriteBatch.setColor(Color.WHITE); // Limpa a cor para o desenho dos ícones

            // RENDERIZAÇÃO DOS ÍCONES (Tamanho aumentado para 0.6f)
            float iconSize = 1.8f;
            float iconOffsetX = (btnRetomar.width - iconSize) / 2;
            float iconOffsetY = (btnRetomar.height - iconSize) / 2;

            // Ícone RETOMAR
            spriteBatch.draw(iconResume,
                btnRetomar.x + iconOffsetX, btnRetomar.y + iconOffsetY,
                iconSize, iconSize);

            // Ícone REINICIAR
            spriteBatch.draw(iconRestart,
                btnReiniciar.x + iconOffsetX, btnReiniciar.y + iconOffsetY,
                iconSize, iconSize);

            // Ícone SAIR
            spriteBatch.draw(iconExit,
                btnSair.x + iconOffsetX, btnSair.y + iconOffsetY,
                iconSize, iconSize);
        }

        spriteBatch.setColor(Color.WHITE);
        spriteBatch.end();
    }

    private void reiniciarJogo() {
        dispose();
        create();
        jogoPausado = false;
        score = 0; // Zera a pontuação
        backgroundMusic.play();
    }

    private void retomarJogo() {
        jogoPausado = false;
    }

    private void sairDoJogo() {
        Gdx.app.exit();
    }

    private void input(float delta) {
        float velocidade = 2f;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            personagem.moverDireita(delta, velocidade);
            fundo.moverDireita(delta, velocidade);
        } else {
            personagem.idle();
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            personagem.pular();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (fundo != null) fundo.dispose();
        if (personagem != null) personagem.dispose();
        if (fantasmas != null) {
            for (Fantasma f : fantasmas) f.dispose();
            fantasmas.clear();
        }
        if (esqueletosVoadores != null) {
            for (EsqueletoVoador e : esqueletosVoadores) e.dispose();
            esqueletosVoadores.clear();
        }

        if (fundoBrilho != null) fundoBrilho.dispose();
        if (texturaBotao != null) texturaBotao.dispose();

        if (iconResume != null) iconResume.dispose();
        if (iconRestart != null) iconRestart.dispose();
        if (iconExit != null) iconExit.dispose();

        if (fontScore != null) fontScore.dispose();

        // NOVO: Limpeza da Textura de Game Over
        if (texturaGameOver != null) texturaGameOver.dispose();
        if (backgroundMusic != null) backgroundMusic.dispose();

        if (fases != null) {
            for (Texture t : fases) t.dispose();

        }
    }
}
