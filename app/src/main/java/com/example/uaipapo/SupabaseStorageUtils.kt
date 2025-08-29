package com.example.uaipapo/*package com.example.uaipapo

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import java.util.UUID

/**
 * Classe utilitária para gerenciar uploads de imagens para o Supabase Storage, gerando uma URL pública para a imagem.
 *
 * @param context O contexto do aplicativo, necessário para acessar a URI de conteúdo.
 */
class SupabaseStorageUtils(val context: Context) {

    /**
     * Inicializa o cliente Supabase.
     * `createSupabaseClient` é uma função de fábrica que configura a conexão.
     * O módulo `Storage` é instalado para habilitar as operações de armazenamento de arquivos.
     * Os placeholders de URL e chave de API devem ser substituídos pelos valores reais.
     */
    val supabase = createSupabaseClient(
        "",
        ""
    ) {
        install(Storage)
    }

    /**
     * Faz o upload de uma imagem para o Supabase Storage.
     *
     * @param uri A URI da imagem a ser upada.
     * @return A URL pública da imagem após o upload, ou `null` se houver um erro.
     */
    suspend fun uploadImage(uri: Uri): String? {
        try {
            // Extrai a extensão do arquivo da URI. Se não for possível, assume "jpg".
            val extension = uri.path?.substringAfterLast(".") ?: "jpg"
            // Gera um nome de arquivo único para evitar colisões.
            val fileName = "${UUID.randomUUID()}.$extension"

            // Abre um InputStream a partir da URI para ler o conteúdo do arquivo.
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            // Lê todos os bytes do InputStream.
            val fileBytes = inputStream.readBytes()

            // Utiliza o cliente Supabase para fazer o upload do arquivo para o bucket.
            supabase.storage.from(BUCKET_NAME).upload(fileName, fileBytes)

            // Obtém a URL pública do arquivo recém-upado.
            val publicUrl = supabase.storage.from(BUCKET_NAME).publicUrl(fileName)
            return publicUrl

        } catch (e: Exception) {
            // Em caso de erro, imprime o stack trace para depuração.
            e.printStackTrace()
            // Retorna `null` para indicar o erro.
            return null
        }
    }

    /**
     * Objeto complementar que armazena constantes globais da classe.
     */
    companion object {
        // Nome do bucket no Supabase Storage onde as imagens serão armazenadas.
        const val BUCKET_NAME = "uaipapo_images"
    }
}*/