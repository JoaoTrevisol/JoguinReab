package br.mackenzie;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
    SpriteBatch spriteBatch;
    FitViewport viewport;

    Fundo fundo;
    Personagem personagem;

    // Inimigos de Chão (Fantasma)
    Array<Fantasma> fantasmas;
    float tempoGerarFantasma = 0f;
    float intervaloGeracaoFantasma = 3f;
    float velocidadeFantasma = 2.5f;

    // --- NOVO: Inimigos Aéreos (Esqueleto Voador) ---
    Array<EsqueletoVoador> esqueletosVoadores;
    float tempoGerarEsqueleto = 0f;
    float intervaloGeracaoEsqueleto = 5f; // Gera a cada 5 segundos
    float velocidadeEsqueleto = 3.0f; // Um pouco mais rápido que o fantasma
    final float ALTURA_VOO = 3.0f; // Altura fixa no mundo (entre 0 e 5)
    // --------------------------------------------------

    boolean personagemAtivo = true;

    // Controle de brilho
    private Texture fundoBrilho;
    private float brilho = 1f; // 1 = tela clara, 0 = escura
    private final float velocidadeEscurecer = 0.3f;
    private final float velocidadeClarear = 0.5f;
    
    // Variáveis para Exibição da Fase
    private Texture[] fases;
    private boolean exibirFase = false;
    private float tempoExibicaoFase = 0f;
    private final float duracaoExibicao = 3f; // 3 segundos
    private int faseAtualParaExibir = 0;


    @Override
    public void create() {
        if (spriteBatch == null) spriteBatch = new SpriteBatch();
        if (viewport == null) viewport = new FitViewport(8, 5);

        fundo = new Fundo();
        personagem = new Personagem();
        personagem.centralizar(viewport);

        fantasmas = new Array<>();
        // NOVO: Inicializa o array do esqueleto voador
        esqueletosVoadores = new Array<>();
        
        tempoGerarFantasma = 0f;
        tempoGerarEsqueleto = 1f; // Inicia a geração do esqueleto 1s depois para evitar sobreposição imediata
        personagemAtivo = true;

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
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Reinicia o jogo ao apertar R
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            reiniciarJogo();
            return;
        }

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
        
        // Atualiza o brilho (S = clareia)
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            brilho += velocidadeClarear * delta;
        } else {
            brilho -= velocidadeEscurecer * delta;
        }
        brilho = MathUtils.clamp(brilho, 0f, 1f);

        // Aumenta velocidade conforme troca o fundo
        switch (fundo.getIndiceAtual()) {
            case 0: 
                velocidadeFantasma = 3.5f; 
                velocidadeEsqueleto = 4.0f;
                intervaloGeracaoFantasma = 3f;
                intervaloGeracaoEsqueleto = 5f;
                break;
            case 1: 
                velocidadeFantasma = 4.5f; 
                velocidadeEsqueleto = 5.5f;
                intervaloGeracaoFantasma = 2.5f;
                intervaloGeracaoEsqueleto = 4f;
                break;
            case 2: 
                velocidadeFantasma = 6f; 
                velocidadeEsqueleto = 7.0f;
                intervaloGeracaoFantasma = 2f;
                intervaloGeracaoEsqueleto = 3f;
                break;
        }

        // --- Geração e Atualização de Fantasmas (Inimigos de Chão) ---
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
            }

            if (f.saiuDaTela()) {
                f.dispose();
                fantasmas.removeIndex(i);
            }
        }

        // --- NOVO: Geração e Atualização de Esqueletos Voadores (Inimigos Aéreos) ---
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
        // --------------------------------------------------------------------------

        // Renderização
        ScreenUtils.clear(Color.WHITE);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        spriteBatch.begin();
        fundo.render(spriteBatch, viewport);
        if (personagemAtivo) personagem.render(spriteBatch);
        
        // Renderiza os inimigos de chão e aéreos
        for (Fantasma f : fantasmas) f.render(spriteBatch);
        for (EsqueletoVoador e : esqueletosVoadores) e.render(spriteBatch);

        // Camada escura (controle de brilho)
        spriteBatch.setColor(0, 0, 0, 1f - brilho);
        spriteBatch.draw(fundoBrilho, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        spriteBatch.setColor(Color.WHITE);
        
        // Desenha a Fase no centro da tela
        if (exibirFase) {
            Texture faseTextura = fases[faseAtualParaExibir];
            float faseLargura = 6f; 
            float faseAltura = 2f; 
            
            float faseX = (viewport.getWorldWidth() - faseLargura) / 2;
            float faseY = (viewport.getWorldHeight() - faseAltura) / 2;

            spriteBatch.draw(faseTextura, faseX, faseY, faseLargura, faseAltura);
        }

        spriteBatch.end();
    }

    private void reiniciarJogo() {
        dispose();
        create();
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
        // NOVO: Limpeza dos esqueletos voadores
        if (esqueletosVoadores != null) {
            for (EsqueletoVoador e : esqueletosVoadores) e.dispose();
            esqueletosVoadores.clear();
        }

        if (fundoBrilho != null) fundoBrilho.dispose();
        
        if (fases != null) {
            for (Texture t : fases) t.dispose();
        }
    }
}