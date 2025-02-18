import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlAnalyzer {
    public static void main(String[] args) {
        // Verifica se somente um parâmetro foi passado
        if (args.length != 1) {
            System.out.println("Uma url deve ser passado por parâmetro");
            System.out.println("Exemplo: java HtmlAnalyzer <url>");
            return;
        }

        String url = args[0];

        try {
            String conteudoHtml = pegarHTML(url);

            if (htmlInvalido(conteudoHtml)) {
                System.out.println("malformed HTML");
                return;
            }
            // System.out.println(conteudoHtml);
            String texto = textoMaisInterno(conteudoHtml);
            System.out.println(texto);
        } catch (Exception e) {
            System.out.println("URL connection error");
        }
    }

    private static String pegarHTML(String urlEnviada) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlEnviada);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String linha;
            Integer numLinha = 1;
            while ((linha = reader.readLine()) != null) {
                result.append(linha);
                numLinha++;
            }
            reader.close();
        }
        return result.toString();
    }

    public static boolean htmlInvalido(String html) {
        Stack<String> tagStack = new Stack<>();
        // Regular expression que busca uma tag html (abertura e fechamento)
        Pattern pattern = Pattern.compile("</?([a-zA-Z0-9]+)[^>]*>");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String tag = matcher.group(1);
            boolean tagFechamento = matcher.group().startsWith("</");

            if (tagFechamento) {
                if (tagStack.isEmpty() || !tagStack.peek().equals(tag)) {
                    return true;
                }
                tagStack.pop();
            } else {
                tagStack.push(tag);
            }
        }

        if (tagStack.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public static String textoMaisInterno(String html) {
        // https://regex101.com/r/dvKE3C/1
        Pattern pattern = Pattern.compile("<(?!/)([^\">]+)>|</([^>]+)>|([^<>]+)");
        Matcher matcher = pattern.matcher(html);
        int nivelAtual = 0;
        int maiorNivel = 0;
        String texto = "";
        while (matcher.find()) {
            if (matcher.group(1) != null) { // tag de abertura
                nivelAtual++;
            } else if (matcher.group(2) != null) { // tag de fechamento
                nivelAtual--;
            } else if (matcher.group(3) != null) { // Conteudo
                if (nivelAtual > maiorNivel) {
                    maiorNivel = nivelAtual;
                    texto = matcher.group(3).trim();
                }
            }
        }
        return texto;
    }
}