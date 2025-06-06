package com.restaurante01.api_restaurante.services;
import com.restaurante01.api_restaurante.conversoes.ProdutoParaProdutoDTO;
import com.restaurante01.api_restaurante.dto.ProdutoDTO;
import com.restaurante01.api_restaurante.entitys.Produto;
import com.restaurante01.api_restaurante.excepetions.PrecoProdutoNegativoException;
import com.restaurante01.api_restaurante.excepetions.ProdutoPossuiHistorico;
import com.restaurante01.api_restaurante.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProdutoService {
    @Autowired
    public ProdutoRepository produtoRepository;

    public List<ProdutoDTO> listarTodosProdutos() {
        return ProdutoParaProdutoDTO.converterParaProduto(produtoRepository.findAll());
    }

    public List<ProdutoDTO> listarProdutosDisponiveis(){
        return ProdutoParaProdutoDTO.converterParaProduto(produtoRepository.findByDisponibilidade(true));
    }

    public List<ProdutoDTO> listarProdutosIndisponiveis() {
        return ProdutoParaProdutoDTO.converterParaProduto(produtoRepository.findByDisponibilidade(false));
    }

    /* método com streans e lambda */
    public List<ProdutoDTO> listarProdutosComQntdBaixa(){
        return ProdutoParaProdutoDTO.converterParaProduto(produtoRepository.findByQuantidadeAtualLessThan(11));
    }


    public ProdutoDTO adicionarNovoProduto(ProdutoDTO produtoDTO) {
        if(produtoDTO.getQuantidadeAtual() < 0 ){
            throw new IllegalArgumentException("A quantidade do produto não pode ser negativa");
        }

            Produto produto = new Produto();
            produto.setNome(produtoDTO.getNome());
            produto.setPreco(produtoDTO.getPreco());
            produto.setDescricao(produtoDTO.getDescricao());
            produto.setDisponibilidade(produtoDTO.getDisponibilidade());
            produto.setQuantidadeAtual(produtoDTO.getQuantidadeAtual());

            Produto produtoSalvo = produtoRepository.save(produto);

            ProdutoDTO produtoDTOretornado = new ProdutoDTO(
                    produtoSalvo.getId(),
                    produtoSalvo.getNome(),
                    produtoSalvo.getDescricao(),
                    produtoSalvo.getPreco(),
                    produtoSalvo.getQuantidadeAtual(),
                    produtoSalvo.getDisponibilidade()
            );
            return produtoDTOretornado;
        }

    public List<ProdutoDTO> atualizarVariosProdutos(List<ProdutoDTO> produtosParaAtualizarDTO) {
        // Mapeia os DTOs usando o ID como chave
        Map<Long, ProdutoDTO> idsMap = produtosParaAtualizarDTO.stream()
                .collect(Collectors.toMap(ProdutoDTO::getId, dto -> dto));

        // Busca os produtos reais do banco que têm os IDs presentes no Map
        List<Produto> produtosEncontrados = produtoRepository.findAllById(idsMap.keySet());

        // Atualiza os produtos encontrados com os dados do DTO correspondente
        for (Produto produto : produtosEncontrados) {
            ProdutoDTO produtoAtualizado = idsMap.get(produto.getId());

            produto.setNome(produtoAtualizado.getNome());
            produto.setDescricao(produtoAtualizado.getDescricao());
            produto.setPreco(produtoAtualizado.getPreco());
            produto.setDisponibilidade(produtoAtualizado.getDisponibilidade());
            produto.setQuantidadeAtual(produtoAtualizado.getQuantidadeAtual());
        }

        //  Salva todos os produtos atualizados no banco de uma vez só
        produtoRepository.saveAll(produtosEncontrados);

        //  Converte de volta os Produtos em DTOs para retornar como resposta
        return ProdutoParaProdutoDTO.converterParaProduto(produtosEncontrados);
    }

        public Produto atualizarProduto(long id, Produto produtoAtualizado) {
        Produto produtoModificado = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        if (produtoAtualizado.getPreco() < 0) {
            throw new PrecoProdutoNegativoException("Preço não pode ser negativo");
        }
        produtoModificado.setNome(produtoAtualizado.getNome());
        produtoModificado.setDescricao(produtoAtualizado.getDescricao());
        produtoModificado.setPreco(produtoAtualizado.getPreco());
        produtoModificado.setQuantidadeAtual(produtoAtualizado.getQuantidadeAtual());
        produtoModificado.setDisponibilidade(produtoAtualizado.getDisponibilidade());
        return produtoRepository.save(produtoModificado);
    }

    public Produto deletarProduto(long id) {
        try {
            Produto produtoSerDeletado = produtoRepository.findById(id).orElseThrow(() ->
                    new RuntimeException("Produto não encontrado"));
            produtoRepository.delete(produtoSerDeletado);
            return produtoSerDeletado;
        } catch (DataIntegrityViolationException e) {
            throw new ProdutoPossuiHistorico("Este produto possui histórico/vinculo com ItensPedidos, se fosse excluido perderiamos os dados deste histórico");
        }
    }
}


