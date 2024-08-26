package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.dto.SerieCleanDTO;
import br.com.alura.screenmatch.model.Categoria;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.model.Serie;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;

public class Principal {

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    ModelMapper modelMapper = new ModelMapper();
    private Optional<Serie> serieBusca;
    private SerieRepository repositorio;
    private List<Serie> series = new ArrayList<>();

    public Principal(SerieRepository repositorio) {
        this.repositorio = repositorio;
    }

    public void exibeMenu() {
        var opcao = -1;
        while (opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar series pelo nome
                    5- Buscar series por ator
                    6 - Buscar top 5 series
                    7- Buscar series pela categoria, dinamismo
                    8- buscar pelo numero maximo de temporadas e avaliacao
                    9- Buscar episodio por trecho
                    10- Top episódios Por Serie

                    0 - Sair
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;

                case 4:
                    buscarSeriesPeloTitulo();
                    break;

                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5();
                    break;
                case 7:
                    buscarSeriesPorCategoria();
                    break;
                case 8:
                    buscarSeriesPorTempEAvaliacao();
                    break;
                case 9:
                    buscarEpisodioPorTrecho();
                    break;
                case 10:
                    topEpisodiosPorSerie();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        // dadosSeries.add(dados);
        repositorio.save(serie);
        System.out.println(dados);
    }

    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    private void buscarEpisodioPorSerie() {
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome");
        var nomeSerie = leitura.nextLine();

        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(
                        ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repositorio.save(serieEncontrada);
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void listarSeriesBuscadas() {
        series = repositorio.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    private void buscarSeriesPeloTitulo() {
        System.out.println("Escolha uma série pelo nome");
        String nomeSerie = leitura.nextLine();
        Optional<Serie> serie = repositorio.findByTituloContainingIgnoreCase(nomeSerie);
        if (serie.isPresent()) {
            System.out.println("dados da serie: " + serie.get());
        } else {
            System.out.println("Série não encontrada!");
        }

        serieBusca = serie;
    }

    private void buscarSeriesPorAtor() {
        System.out.println("Escolha um ator");
        String nomeAutor = leitura.nextLine();
        System.out.println("Avaliação a partir de qual pontuação?");
        double avaliacao = leitura.nextDouble();
        List<Optional<Serie>> serie = repositorio
                .findByatoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAutor, avaliacao);
        if (!serie.isEmpty()) {
            serie.forEach(s -> System.out.println("dados da serie: " + s.get() + "\n"));
        } else {
            System.out.println("Série não encontrada!");
        }
    }

    private void buscarTop5() {
        List<Serie> top5 = repositorio.findTop5ByOrderByAvaliacaoDesc();
        List<SerieCleanDTO> top5Clean = top5.stream().map(s -> modelMapper.map(s, SerieCleanDTO.class)).toList();
        top5Clean.forEach(sc -> System.out.println("Series buscadas: " + sc));
    }

    private void buscarSeriesPorCategoria() { // ajuste
        System.out.println("Deseja buscar séries de que categoria/gênero? ");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria = repositorio.findByGenero(categoria);
        System.out.println("Séries da categoria " + nomeGenero);
        seriesPorCategoria.forEach(System.out::println);
    }

    private void buscarSeriesPorTempEAvaliacao() {
        System.out.println("Informe o numero maximo de temporadas: ");
        Integer temporadasMax = leitura.nextInt();
        System.out.println("Informa agora a avaliação minima, ex: 8,0");
        Double AvaliacaoMin = leitura.nextDouble();

        // List<Serie> seriesFiltradas = repositorio
        // .findByTotalTemporadasLessThanEqualAndAvaliacaoGreaterThanEqual(temporadasMax,
        // AvaliacaoMin);
        List<Serie> seriesFiltradas = repositorio.seriePorTemporadaEAvaliacao(temporadasMax, AvaliacaoMin);
        seriesFiltradas.forEach(s -> System.out.println(s));
    }

    private void buscarEpisodioPorTrecho() {
        System.out.println("Qual nome do eposido?");
        String techoEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrados = repositorio.episodiosPorTrecho(techoEpisodio);
        episodiosEncontrados.forEach(System.out::println);
    }

    private void topEpisodiosPorSerie() {
        buscarSeriesPeloTitulo();
        if (serieBusca.isEmpty())
            return;
        Serie serie = serieBusca.get();

        List<Episodio> topEpisodios = repositorio.topEpisodiosPorSerie(serie).stream().limit(5)
                .collect(Collectors.toList());
        topEpisodios.forEach(System.out::println);
    }

}
