package com.controleestoque.api_estoque.controller;
 
import java.util.List;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
 
import com.controleestoque.api_estoque.model.Produto;
import com.controleestoque.api_estoque.repository.ProdutoRepository;
 
import lombok.RequiredArgsConstructor;
 
import com.controleestoque.api_estoque.repository.CategoriaRepository;
import com.controleestoque.api_estoque.repository.FornecedorRepository;
 
@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {
 
    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final FornecedorRepository fornecedorRepository;
 
    // Estoque é geralmente manipulado via Produto ou separadamente.
 
    // GET /api/produtos
    @GetMapping
    public List<Produto> getAllProdutos() {
        // Retorna a lista de produtos. Pode ser necessário configurar DTOs para evitar loops infinitos com JSON.
        return produtoRepository.findAll();
    }
 
    // GET /api/produtos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Produto> getCategoriaById(@PathVariable Long id) {
        // Busca a categoria pelo ID. Usa orElse para retornar 404 se não encontrar.
        return produtoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
 
 // POST /api/produtos
// Neste método, assumimos que a Categoria e os Fornecedores já existem
// e seus IDs são passados no corpo da requisição (ProdutoDTO seria o ideal aqui).
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
public ResponseEntity<Produto> createProduto(@RequestBody Produto produto) {
 
 // 1. Gerenciamento do 1:N (Categoria)
 // A categoria deve ser buscada para garantir que existe e está no contexto de persistência.
 if (produto.getCategoria() == null || produto.getCategoria().getId() == null) {
 return ResponseEntity.badRequest().build(); // Categoria é obrigatória
 }
 
 categoriaRepository.findById(produto.getCategoria().getId())
 .ifPresent(produto::setCategoria); // Associa a categoria gerenciada
 
 // 2. Gerenciamento do N:M (Fornecedores)
 // Busca todos os fornecedores pelos IDs fornecidos
 if (produto.getFornecedores() != null && !produto.getFornecedores().isEmpty()) {
 
 // Cria um Set para armazenar os fornecedores gerenciados
 produto.getFornecedores().clear();
 
 // Aqui, em um projeto real, você buscaria os Fornecedores um por um
 // ou usando um método customizado do repositório.
 // Exemplo Simplificado:
 produto.getFornecedores().forEach(fornecedor -> {
 fornecedorRepository.findById(fornecedor.getId())
 .ifPresent(produto.getFornecedores()::add); // Adiciona o fornecedor gerenciado
 });
 }
 
 // 3. Salva o Produto (e o Estoque, se o CASCADE estiver configurado)
 Produto savedProduto = produtoRepository.save(produto);
 
 return ResponseEntity.status(HttpStatus.CREATED).body(savedProduto);
}
 
 
// PUT /api/produtos/{id}
@PutMapping("/{id}")
public ResponseEntity<Produto> updateProduto(@PathVariable Long id, @RequestBody Produto produtoDetails) {
 
 return produtoRepository.findById(id)
 .map(produto -> {
 
 // Atualiza os dados do produto encontrado
 produto.setNome(produtoDetails.getNome());
 
 Produto updatedProduto = produtoRepository.save(produto);
 return ResponseEntity.ok(updatedProduto);
 
 })
 .orElse(ResponseEntity.notFound().build());
}
 
 
// DELETE /api/produtos/{id}
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteProduto(@PathVariable Long id) {
 
 // Tenta encontrar e deletar
 if (!produtoRepository.existsById(id)) {
 return ResponseEntity.notFound().build();
 }
 
 produtoRepository.deleteById(id);
 return ResponseEntity.noContent().build(); // Retorna código 204 (No Content)
}
 
