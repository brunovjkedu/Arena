
package br.uffs.cc.jarena;

/**
 * Agente da equipe AxenteFaiz
 * 
 * Integrantes:
 *  - Deborah Ely
 *  - Kauã Perosso
 *  
 * Estratégia:
 *  O agente se movimenta até encontrar um cogumelo, quando encontrado o agente se multiplica


public class AgenteAxenteFaiz extends Agente {

    private static boolean inimigosDerrotados = false;

    private boolean jaDividiu = false;
    private static int totalAgentes = 0;
    private static final int MAX_AGENTES = 30;


    private Integer alvoX = null;
    private Integer alvoY = null;

    private final int centroX = 25;
    private final int centroY = 25;
    private boolean indoParaCentro = true;

    public AgenteAxenteFaiz(Integer x, Integer y, Integer energia) {
        super(x, y, energia);
        setDirecao(geraDirecaoAleatoria());
        totalAgentes++;
    }

    @Override
    public void pensa() {
        if (getEnergia() < 30) {
            para();
            return;
        }

        if (inimigosDerrotados) {
        para();
        return;
    }

    
        if (indoParaCentro) {
            moverParaCoordenada(centroX, centroY);

public class AgenteAxenteFaiz extends Agente {
    private boolean parado = false;       
    private boolean explorador = true;     
    private boolean souBasePrincipal = false; 
    private Integer alvoX = null;          
    private Integer alvoY = null;

    private int idBasePrincipal = -1;      

    public AgenteAxenteFaiz(Integer x, Integer y, Integer energia) {
        super(x, y, energia);

        if (Math.random() < 0.05) {
            parado = true;
            explorador = false;
            System.out.println("[AxenteFaiz " + getId() + "] Sou uma base inicial.");
        } else {
            setDirecao(geraDirecaoAleatoria());
            System.out.println("[AxenteFaiz " + getId() + "] Sou explorador.");
        }
    }

 

            if (Math.abs(getX() - alvoX) <= 1 && Math.abs(getY() - alvoY) <= 1) {
                alvoX = null;
                alvoY = null;
            }
        } else {
            if (!podeMoverPara(getDirecao())) {
                setDirecao(geraDirecaoAleatoria());
            }
          
    private void moverParaCoordenada(int destinoX, int destinoY) {
        int dx = destinoX - getX();
        int dy = destinoY - getY();
        if (dx == 0 && dy == 0) return;

        if (Math.abs(dx) > Math.abs(dy)) {
            direcao = dx > 0 ? DIREITA : ESQUERDA;
        } else {
            direcao = dy > 0 ? BAIXO : CIMA;
        }

        if (podeMoverPara(direcao)) setDirecao(direcao);
    }

    private int distanciaPara(int x, int y) {
        return (int) Math.sqrt(Math.pow(getX() - x, 2) + Math.pow(getY() - y, 2));
    }


    @Override
    public void recebeuEnergia() {
        if (!explorador) return;

        String msg = "COGUMELO:" + getId() + ":" + getX() + ":" + getY() + ":" + getEnergia();
        enviaMensagem(msg);
        System.out.println("[AxenteFaiz " + getId() + "] Achei cogumelo! " + msg);

    }

    @Override
    public void recebeuMensagem(String msg) {
        if (msg == null) return;

        if (msg.startsWith("NOVA_BASE:")) {
            try {
                int idNovaBase = Integer.parseInt(msg.split(":")[1]);
                idBasePrincipal = idNovaBase;

                if (getId() != idNovaBase && souBasePrincipal) {
                    souBasePrincipal = false;
                    explorador = true;
                    parado = false;
                    System.out.println("[AxenteFaiz " + getId() + "] Base transferida. Voltando a explorar.");
                }

            } catch (Exception e) {
            }
            return;
        }

        if (msg.startsWith("COGUMELO:")) {
            String[] partes = msg.split(":");
            if (partes.length != 5) return;

            try {
                int remetenteId = Integer.parseInt(partes[1]);
                int cx = Integer.parseInt(partes[2]);
                int cy = Integer.parseInt(partes[3]);
                int energiaRemetente = Integer.parseInt(partes[4]);

                if (getEnergia() > energiaRemetente && !souBasePrincipal) {
                    souBasePrincipal = true;
                    parado = true;
                    explorador = false;
                    idBasePrincipal = getId();

                    enviaMensagem("NOVA_BASE:" + getId());
                    System.out.println("[AxenteFaiz " + getId() + "] Virei a nova base principal!");

                } else if (!souBasePrincipal) {
                    alvoX = cx;
                    alvoY = cy;
                }

            } catch (NumberFormatException e) {
            }

      
      
    @Override
    public void tomouDano(int energiaRestanteInimigo) {
    }

    @Override
    public void ganhouCombate() {
        enviaMensagem("INIMIGO_MORTO");

        inimigosDerrotados = false;
    }

    @Override

    public void tomouDano(int energiaRestanteInimigo) {
        // comportamento padrão: ignora
    }

    @Override
    public void ganhouCombate() {
        // comportamento padrão: ignora
    }


}

