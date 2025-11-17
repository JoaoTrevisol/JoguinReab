package br.mackenzie;
 

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class EsqueletoVoador {

    // Ajuste esses valores para o tamanho exato dos seus frames
    private static final int FRAME_COLS = 3; // Supondo 3 frames na sua sprite sheet
    private static final int FRAME_ROWS = 1; // Supondo 1 linha na sua sprite sheet
    private static final float FRAME_DURATION = 0.1f;

    // --- Fatores de Ajuste da Hitbox ---
    private static final float HITBOX_LARGURA_FATOR = 0.7f;
    private static final float HITBOX_ALTURA_FATOR = 0.5f; // Mais fino para um inimigo aéreo

    private Animation<TextureRegion> animacao;
    private float stateTime;
    private TextureRegion frameAtual;

    private float x, y;
    private float largura = 2.0f; // Ajuste o tamanho da sprite no mundo
    private float altura = 2.0f; // Ajuste o tamanho da sprite no mundo
    private float velocidade;
    private Rectangle hitbox;

    private final float hitboxLargura;
    private final float hitboxAltura;
    private final float offsetX;
    private final float offsetY;

    public EsqueletoVoador(FitViewport viewport, float yVoando, float velocidade) {
        this.y = yVoando;
        this.velocidade = velocidade;

       // --- Configuração da Animação do Esqueleto Voador ---
// Ajuste o número de frames (imagens separadas) que você tem
TextureRegion[] frames = new TextureRegion[2]; // Supondo que você tenha 4 frames: EsqueletoVoador1.png a EsqueletoVoador4.png
for (int i = 0; i < 2; i++) {
    // Certifique-se de que os nomes dos seus arquivos de imagem sigam este padrão
    Texture t = new Texture("EsqueletoVoador" + (i + 1) + ".png");
    frames[i] = new TextureRegion(t);
}
// 0.1f é a duração de cada frame (10 frames por segundo)
animacao = new Animation<>(0.1f, frames); 
animacao.setPlayMode(Animation.PlayMode.LOOP);

stateTime = 0f;
frameAtual = animacao.getKeyFrame(stateTime);

        animacao = new Animation<>(FRAME_DURATION, frames);
        animacao.setPlayMode(Animation.PlayMode.LOOP);

        stateTime = 0f;
        frameAtual = animacao.getKeyFrame(stateTime);
        
        // A posição X inicial deve ser fora da tela, à direita
        this.x = viewport.getWorldWidth() + largura;

        // --- CÁLCULO E DEFINIÇÃO DA HITBOX ---
        this.hitboxLargura = largura * HITBOX_LARGURA_FATOR;
        this.hitboxAltura = altura * HITBOX_ALTURA_FATOR;
        this.offsetX = (largura - hitboxLargura) / 2;
        this.offsetY = (altura - hitboxAltura) / 2; // Centraliza verticalmente

        this.hitbox = new Rectangle(
            x + offsetX,
            y + offsetY,
            hitboxLargura,
            hitboxAltura
        );
    }

    public void update(float delta) {
        stateTime += delta;
        frameAtual = animacao.getKeyFrame(stateTime);

        x -= velocidade * delta; // Move o esqueleto para a esquerda

        // ATUALIZAÇÃO DA POSIÇÃO DA HITBOX
        hitbox.setPosition(x + offsetX, y + offsetY);
    }

    public void render(SpriteBatch batch) {
        // Desenha o frame atual da animação (assumindo que o sprite sheet já está virado para a esquerda)
        batch.draw(frameAtual, x, y, largura, altura);
    }

    public boolean saiuDaTela() {
        return x + largura < 0;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void dispose() {
        // A sprite sheet inteira pode ser liberada uma vez, se a Textura for a mesma.
        // Se cada frame for uma Texture separada (como na sua classe Fantasma), use o código abaixo.
        // Para Sprite Sheet (melhor performance), libere apenas a textura principal (spriteSheet).
        
        // Como o Fantasma.java usa texturas separadas, vamos seguir o padrão
        for (TextureRegion t : animacao.getKeyFrames()) {
            t.getTexture().dispose();
        }
    }
}