package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Fundo {
    private Texture[] fundos;
    private int indiceAtual = 0;
    private float tempoTroca = 10f; // troca a cada 10 segundos
    private float tempoAtual = 0f;

    private float offsetX = 0f;
    private float velocidadeParalaxe = 1f;
    
    // NOVO: Indica se houve uma troca de fundo neste frame
    private boolean trocouFundo = false;

    public Fundo() {
        fundos = new Texture[]{
            new Texture("tela_fundo1.png"),
            new Texture("tela_fundo2.png"),
            new Texture("tela_fundo3.png")
        };
    }

    public void atualizar(float delta) {
        trocouFundo = false; // Reinicia o indicador a cada frame
        tempoAtual += delta;
        if (indiceAtual < fundos.length - 1 && tempoAtual >= tempoTroca) {
            tempoAtual = 0;
            indiceAtual++;
            trocouFundo = true; // Define como true se a troca ocorreu
        }
    }

    public void moverDireita(float delta, float velocidade) {
        offsetX -= velocidade * delta * velocidadeParalaxe;
    }

    public void moverEsquerda(float delta, float velocidade) {
        offsetX += velocidade * delta * velocidadeParalaxe;
    }

    public void render(SpriteBatch batch, FitViewport viewport) {
        float largura = viewport.getWorldWidth();
        float altura = viewport.getWorldHeight();
        float x1 = offsetX % largura;
        if (x1 > 0) x1 -= largura;

        Texture fundo = fundos[indiceAtual];
        batch.draw(fundo, x1, 0, largura, altura);
        batch.draw(fundo, x1 + largura, 0, largura, altura);
    }

    public int getIndiceAtual() {
        return indiceAtual;
    }
    
    // NOVO: Getter para verificar se houve troca
    public boolean getTrocouFundo() {
        return trocouFundo;
    }

    public void dispose() {
        for (Texture f : fundos) f.dispose(); }
    }
