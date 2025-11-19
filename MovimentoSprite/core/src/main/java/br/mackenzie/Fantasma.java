package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Fantasma {

    // --- Fatores de Ajuste da Hitbox ---
    // A hitbox terá 60% da largura total da sprite
    private static final float HITBOX_LARGURA_FATOR = 0.6f;
    // A hitbox terá 80% da altura total da sprite
    private static final float HITBOX_ALTURA_FATOR = 0.8f;

    private Animation<TextureRegion> animacao;
    private float stateTime;
    private TextureRegion frameAtual;

    private float x, y;
    private float largura = 1.5f;
    private float altura = 1.5f;
    private float velocidade;
    private Rectangle hitbox;

    // Variáveis para armazenar o tamanho e o offset da hitbox
    private final float hitboxLargura;
    private final float hitboxAltura;
    private final float offsetX;
    private final float offsetY;

    public Fantasma(FitViewport viewport, float y, float velocidade) {
        this.y = y;
        this.velocidade = velocidade;

        // --- Configuração da Animação da Caveira ---
        TextureRegion[] frames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            Texture t = new Texture("Caveira" + (i + 1) + ".png");
            frames[i] = new TextureRegion(t);
        }
        animacao = new Animation<>(0.1f, frames);
        animacao.setPlayMode(Animation.PlayMode.LOOP);

        stateTime = 0f;
        frameAtual = animacao.getKeyFrame(stateTime);

        // A posição X inicial deve ser fora da tela, à direita
        this.x = viewport.getWorldWidth() + largura;

        // --- CÁLCULO E DEFINIÇÃO DA HITBOX MENOR ---

        // 1. Calcula o novo tamanho da hitbox
        this.hitboxLargura = largura * HITBOX_LARGURA_FATOR;
        this.hitboxAltura = altura * HITBOX_ALTURA_FATOR;

        // 2. Calcula o offset (deslocamento) para centralizar a hitbox
        // O offset X centraliza a hitbox horizontalmente
        this.offsetX = (largura - hitboxLargura) / 2;
        // O offset Y move a hitbox levemente para cima (ou ajuste se necessário)
        this.offsetY = altura * 0.1f;

        // 3. Cria o retângulo da hitbox com os offsets iniciais
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

        x -= velocidade * delta; // Move o fantasma para a esquerda

        // --- ATUALIZAÇÃO DA POSIÇÃO DA HITBOX ---
        // A hitbox se move junto com 'x' e 'y', mas mantendo o offset
        hitbox.setPosition(x + offsetX, y + offsetY);
    }

    public void render(SpriteBatch batch) {
        // Desenha o frame atual da animação
        batch.draw(frameAtual, x, y, largura, altura);
    }

    public boolean saiuDaTela() {
        return x + largura < 0;
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void dispose() {
        for (TextureRegion t : animacao.getKeyFrames()) {
            t.getTexture().dispose();
        }
    }
}
