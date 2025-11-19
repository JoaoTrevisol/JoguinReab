package br.mackenzie;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Personagem {
    // VARIÁVEIS DE ANIMAÇÃO
    private Animation<TextureRegion> animacaoCorridaIntro;
    private Animation<TextureRegion> animacaoCorridaLoop;
    private Animation<TextureRegion> animacaoPulo;

    private float stateTimeCorrida;
    private float stateTimePulo;
    private TextureRegion frameAtual;

    private boolean introCorridaTerminou = false;

    // VARIÁVEIS DO PERSONAGEM
    private Rectangle hitbox;
    private float posX, posY;
    private boolean olhandoDireita = true;

    private boolean noChao = true;
    private float velY = 0f;

    // --- 🎯 AJUSTE DE FÍSICA PARA UM PULO MAIS ALTO ---
    // Aumentamos a magnitude da gravidade e, principalmente, a força do pulo.
    // Isso cria um pulo mais longo e alto.
    private final float gravidade = -24f; // De -16f para -22f
    private final float forcaPulo = 13f;  // De 11f para 17f
    // ---------------------------------------------------

    private final float largura = 2f;
    private final float altura = 2f;

    // --- NOVO: AJUSTE DE HITBOX PARA SER MENOR QUE O SPRITE ---
    private static final float HITBOX_LARGURA_FATOR = 0.3f; // 30% da largura
    private static final float HITBOX_ALTURA_FATOR = 0.9f; // 90% da altura
    private final float hitboxLargura;
    private final float hitboxAltura;
    private final float offsetX;
    private final float offsetY;
    // ---------------------------------------------------------


    public Personagem() {
        // ... (preparação dos frames, animações de corrida e pulo inalteradas) ...
        TextureRegion[] framesCompletos = new TextureRegion[8];
        for (int i = 0; i < 8; i++) {
            Texture t = new Texture("Run" + (i + 1) + ".png");
            framesCompletos[i] = new TextureRegion(t);
        }

        TextureRegion[] framesIntro = new TextureRegion[3];
        System.arraycopy(framesCompletos, 0, framesIntro, 0, 3);
        animacaoCorridaIntro = new Animation<>(0.1f, framesIntro);
        animacaoCorridaIntro.setPlayMode(Animation.PlayMode.NORMAL);

        TextureRegion[] framesLoop = new TextureRegion[4];
        System.arraycopy(framesCompletos, 3, framesLoop, 0, 4);
        animacaoCorridaLoop = new Animation<>(0.1f, framesLoop);
        animacaoCorridaLoop.setPlayMode(Animation.PlayMode.LOOP);

        TextureRegion[] framesPulo = new TextureRegion[6];
        for (int i = 0; i < 6; i++) {
            Texture t = new Texture("Jump" + (i + 1) + ".png");
            framesPulo[i] = new TextureRegion(t);
        }
        animacaoPulo = new Animation<>(0.1f, framesPulo);

        frameAtual = framesIntro[0];

        // --- CÁLCULO DA HITBOX MENOR ---
        this.hitboxLargura = largura * HITBOX_LARGURA_FATOR;
        this.hitboxAltura = altura * HITBOX_ALTURA_FATOR;
        this.offsetX = (largura - hitboxLargura) / 2;
        this.offsetY = 0f; // Mantém a base da hitbox no Y=0

        // A hitbox inicial é criada com o offset.
        hitbox = new Rectangle(0 + offsetX, 0 + offsetY, hitboxLargura, hitboxAltura);
    }

    public void centralizar(FitViewport viewport) {
        posX = (viewport.getWorldWidth() - largura) / 3.5f;
        posY = 0;

        // Atualiza a hitbox com o novo tamanho e o offset na posição inicial (posX, posY)
        hitbox.set(posX + offsetX, posY + offsetY, hitboxLargura, hitboxAltura);
    }

    public void update(float delta, FitViewport viewport) {
        if (!noChao) {
            velY += gravidade * delta;
            posY += velY * delta;

            stateTimePulo += delta;
            frameAtual = animacaoPulo.getKeyFrame(stateTimePulo, false);

            if (posY <= 0) {
                posY = 0;
                velY = 0;
                noChao = true;
                introCorridaTerminou = false;
                frameAtual = animacaoCorridaIntro.getKeyFrames()[0];
            }
        }

        // --- ATUALIZAÇÃO DA POSIÇÃO DA HITBOX ---
        // A hitbox se move com o personagem, mas mantém seu offset e tamanho menores
        hitbox.setPosition(posX + offsetX, posY + offsetY);
    }

    // ... (moverDireita, moverEsquerda, idle, pular, render, getHitbox, dispose inalterados) ...

    public void moverDireita(float delta, float velocidade) {
        olhandoDireita = true;
        if (noChao) {
            stateTimeCorrida += delta;

            if (!introCorridaTerminou) {
                frameAtual = animacaoCorridaIntro.getKeyFrame(stateTimeCorrida);

                if (animacaoCorridaIntro.isAnimationFinished(stateTimeCorrida)) {
                    introCorridaTerminou = true;
                }
            }

            if (introCorridaTerminou) {
                frameAtual = animacaoCorridaLoop.getKeyFrame(stateTimeCorrida);
            }
        }
    }

    public void moverEsquerda(float delta, float velocidade) {
        olhandoDireita = false;
        if (noChao) {
            stateTimeCorrida += delta;

            if (!introCorridaTerminou) {
                frameAtual = animacaoCorridaIntro.getKeyFrame(stateTimeCorrida);

                if (animacaoCorridaIntro.isAnimationFinished(stateTimeCorrida)) {
                    introCorridaTerminou = true;
                }
            }

            if (introCorridaTerminou) {
                frameAtual = animacaoCorridaLoop.getKeyFrame(stateTimeCorrida);
            }
        }
    }

    public void idle() {
        if (noChao) {
            stateTimeCorrida = 0f;
            introCorridaTerminou = false;
            frameAtual = animacaoCorridaIntro.getKeyFrames()[0];
        }
    }

    public void pular() {
        if (noChao) {
            velY = forcaPulo;
            noChao = false;
            stateTimePulo = 0f;
            stateTimeCorrida = 0f;
            introCorridaTerminou = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (olhandoDireita) {
            batch.draw(frameAtual, posX, posY, largura, altura);
        } else {
            batch.draw(frameAtual, posX + largura, posY, -largura, altura);
        }
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void dispose() {
        for (TextureRegion t : animacaoCorridaIntro.getKeyFrames()) t.getTexture().dispose();
        for (TextureRegion t : animacaoCorridaLoop.getKeyFrames()) t.getTexture().dispose();
        for (TextureRegion t : animacaoPulo.getKeyFrames()) t.getTexture().dispose();
    }
}
