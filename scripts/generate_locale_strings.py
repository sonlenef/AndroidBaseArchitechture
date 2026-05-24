#!/usr/bin/env python3
"""Generate complete localized strings.xml for es, pt, hi, vi from values/strings.xml."""

from __future__ import annotations

import re
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE_XML = ROOT / "app/src/main/res/values/strings.xml"

SKIP_NAMES = {
    "app_name",
    "language_option_english",
    "language_option_spanish",
    "language_option_portuguese",
    "language_option_hindi",
    "language_option_vietnamese",
}

LOCALES = ("es", "pt", "hi", "vi")

# Professional translations keyed by string name.
# English source of truth is read from values/strings.xml when a key is absent here.
T: dict[str, dict[str, str]] = {}

def tr(name: str, es: str, pt: str, hi: str, vi: str) -> None:
    T[name] = {"es": es, "pt": pt, "hi": hi, "vi": vi}


# --- App & maintenance ---
tr("maintenance_title", "En mantenimiento", "Em manutenção", "रखरखाव जारी", "Đang bảo trì")

# --- Shared UI ---
tr("back", "Atrás", "Voltar", "वापस", "Quay lại")
tr("cancel", "Cancelar", "Cancelar", "रद्द करें", "Hủy")
tr("retry", "Reintentar", "Tentar novamente", "पुनः प्रयास करें", "Thử lại")
tr("content_desc_error", "Error", "Erro", "त्रुटि", "Lỗi")
tr("loading", "Cargando…", "Carregando…", "लोड हो रहा है…", "Đang tải…")

# --- Language UI ---
tr("language_action", "Idioma", "Idioma", "भाषा", "Ngôn ngữ")
tr("language_menu_title", "Elegir idioma", "Escolher idioma", "भाषा चुनें", "Chọn ngôn ngữ")

# --- Scanner ---
tr("scanner_action_scan", "ESCANEAR", "DIGITALIZAR", "स्कैन", "QUÉT")
tr("scanner_processing", "Procesando documento…", "Processando documento…", "दस्तावेज़ संसाधित हो रहा है…", "Đang xử lý tài liệu…")
tr("scanner_processing_page_background", "Añadiendo página…", "Adicionando página…", "पृष्ठ जोड़ा जा रहा है…", "Đang thêm trang…")
tr("scanner_saved_file_label", "Guardado en:", "Salvo em:", "यहाँ सहेजा गया:", "Đã lưu tại:")
tr("scanner_save_success_title", "PDF guardado", "PDF salvo", "PDF सहेजा गया", "Đã lưu PDF")
tr("scanner_save_success_subtitle", "%1$s · %2$s", "%1$s · %2$s", "%1$s · %2$s", "%1$s · %2$s")
tr("scanner_save_success_hint", "Comparte por correo u otras aplicaciones, o guárdalo en Descargas.", "Compartilhe por e-mail ou outros apps, ou salve em Downloads.", "ईमेल या अन्य ऐप से साझा करें, या डाउनलोड में सहेजें।", "Chia sẻ qua email hoặc ứng dụng khác, hoặc lưu vào Thư mục Tải xuống.")
tr("scanner_action_share", "Exportar", "Exportar", "निर्यात", "Xuất file")
tr("scanner_action_email", "Correo", "E-mail", "ईमेल", "Email")
tr("scanner_action_save_downloads", "Guardar en el dispositivo", "Salvar no dispositivo", "डिवाइस में सहेजें", "Lưu vào thiết bị")
tr("scanner_share_chooser_title", "Compartir PDF mediante", "Compartilhar PDF via", "PDF साझा करें", "Chia sẻ PDF qua")
tr("scanner_email_chooser_title", "Enviar PDF por correo", "Enviar PDF por e-mail", "ईमेल से PDF भेजें", "Gửi PDF qua email")
tr("scanner_saved_to_downloads", "Guardado en la carpeta Descargas", "Salvo na pasta Downloads", "डाउनलोड फ़ोल्डर में सहेजा गया", "Đã lưu vào thư mục Tải xuống")
tr("scanner_share_failed", "No se pudo compartir el PDF. Inténtalo de nuevo.", "Não foi possível compartilhar o PDF. Tente novamente.", "PDF साझा नहीं हो सका। कृपया पुनः प्रयास करें।", "Không thể chia sẻ PDF. Vui lòng thử lại.")
tr("scanner_save_downloads_failed", "No se pudo guardar en Descargas. Inténtalo de nuevo.", "Não foi possível salvar em Downloads. Tente novamente.", "डाउनलोड में सहेजा नहीं जा सका। कृपया पुनः प्रयास करें।", "Không thể lưu vào Tải xuống. Vui lòng thử lại.")
tr("scanner_continue_scan", "Seguir escaneando", "Continuar digitalizando", "स्कैन जारी रखें", "Tiếp tục quét")
tr("scanner_back_home", "Volver al inicio", "Voltar ao início", "होम पर वापस जाएँ", "Về trang chủ")
tr("scanner_error_prefix", "Error: %1$s", "Erro: %1$s", "त्रुटि: %1$s", "Lỗi: %1$s")
tr("scanner_error_oops_title", "¡Vaya!", "Ops!", "ओह!", "Rất tiếc!")
tr("scanner_error_empty_pages", "No hay páginas para guardar.", "Não há páginas para salvar.", "सहेजने के लिए कोई पृष्ठ नहीं है।", "Không có trang để lưu.")
tr("scanner_error_save_failed", "No se pudo guardar el PDF. Inténtalo de nuevo.", "Não foi possível salvar o PDF. Tente novamente.", "PDF सहेजा नहीं जा सका। कृपया पुनः प्रयास करें।", "Không thể lưu PDF. Vui lòng thử lại.")
tr("scanner_error_decode_failed", "No se pudo leer la imagen capturada.", "Não foi possível ler a imagem capturada.", "कैप्चर की गई छवि पढ़ी नहीं जा सकी।", "Không đọc được ảnh đã chụp.")
tr("scanner_error_unknown", "Se produjo un error desconocido.", "Ocorreu um erro desconhecido.", "अज्ञात त्रुटि हुई।", "Đã xảy ra lỗi không xác định.")
tr("scanner_error_crop_failed", "No se pudo recortar la imagen. Inténtalo de nuevo.", "Não foi possível recortar a imagem. Tente novamente.", "छवि क्रॉप नहीं हो सकी। कृपया पुनः प्रयास करें।", "Không thể cắt ảnh. Vui lòng thử lại.")
tr("scanner_recent_metadata_failed", "PDF guardado, pero no se actualizó el historial de escaneos.", "PDF salvo, mas o histórico de digitalizações não foi atualizado.", "PDF सहेजा गया, लेकिन हाल के स्कैन अपडेट नहीं हुए।", "Đã lưu PDF nhưng không cập nhật được lịch sử quét.")
tr("scanner_camera_permission_required", "Se requiere permiso de cámara para continuar.", "É necessária permissão da câmera para continuar.", "जारी रखने के लिए कैमरा अनुमति आवश्यक है।", "Cần quyền camera để tiếp tục.")
tr("scanner_capture_again", "Repetir captura", "Capturar novamente", "फिर से कैप्चर करें", "Chụp lại")
tr("scanner_capture_detecting", "Detectando documento…", "Detectando documento…", "दस्तावेज़ पहचाना जा रहा है…", "Đang nhận diện tài liệu…")
tr("scanner_capture_flattening", "Aplanando página…", "Nivelando página…", "पृष्ठ समतल किया जा रहा है…", "Đang làm phẳng trang…")
tr("scanner_capture_saving_page", "Añadiendo a la colección…", "Adicionando à coleção…", "संग्रह में जोड़ा जा रहा है…", "Đang thêm vào bộ sưu tập…")
tr("scanner_save_pdf", "Guardar PDF", "Salvar PDF", "PDF सहेजें", "Lưu PDF")
tr("scanner_flash", "Flash", "Flash", "फ़्लैश", "Đèn flash")
tr("scanner_mode_label", "Modo de escáner", "Modo do scanner", "स्कैनर मोड", "Chế độ quét")
tr("scanner_page_mode_label", "Modo de página", "Modo de página", "पृष्ठ मोड", "Chế độ trang")
tr("scanner_mode_auto", "Automático", "Automático", "स्वचालित", "Tự động")
tr("scanner_mode_manual", "Manual", "Manual", "मैनुअल", "Thủ công")
tr("scanner_mode_single", "Una página", "Página única", "एक पृष्ठ", "Một trang")
tr("scanner_mode_multi", "Varias páginas", "Várias páginas", "कई पृष्ठ", "Nhiều trang")
tr("scanner_frame_auto_hint", "Detectando automáticamente los bordes del documento", "Detectando automaticamente as bordas do documento", "दस्तावेज़ के किनारे स्वचालित रूप से पहचाने जा रहे हैं", "Tự động nhận diện viền tài liệu")
tr("scanner_frame_manual_hint", "Apunta la cámara al documento y toca para capturar", "Aponte a câmera para o documento e toque para capturar", "कैमरा दस्तावेज़ पर रखें और कैप्चर करने के लिए टैप करें", "Hướng camera vào tài liệu và chạm để chụp")
tr("scanner_page_count", "%1$d páginas escaneadas", "%1$d páginas digitalizadas", "%1$d पृष्ठ स्कैन किए गए", "Đã quét %1$d trang")
tr("scanner_crop_title", "Ajustar bordes", "Ajustar bordas", "किनारे समायोजित करें", "Điều chỉnh viền")
tr("scanner_crop_hint", "Arrastra las esquinas para ajustar el área del documento", "Arraste os cantos para ajustar a área do documento", "दस्तावेज़ क्षेत्र समायोजित करने के लिए कोने खींचें", "Kéo các góc để điều chỉnh vùng tài liệu")
tr("scanner_crop_auto_detect", "Detección automática", "Detecção automática", "स्वतः पहचान", "Tự động nhận diện")
tr("scanner_crop_confirm", "Continuar", "Continuar", "जारी रखें", "Tiếp tục")
tr("scanner_crop_retake", "Repetir", "Refazer", "फिर से लें", "Chụp lại")
tr("scanner_filter_title", "Elegir filtro", "Escolher filtro", "फ़िल्टर चुनें", "Chọn bộ lọc")
tr("scanner_filter_original", "Original", "Original", "मूल", "Gốc")
tr("scanner_filter_bw", "Blanco y negro", "Preto e branco", "श्वेत-श्याम", "Đen trắng")
tr("scanner_filter_grayscale", "Escala de grises", "Tons de cinza", "ग्रेस्केल", "Thang xám")
tr("scanner_filter_magic", "Color", "Cor", "रंगीन", "Màu")
tr("scanner_filter_sharpen", "Enfocar", "Nitidez", "तेज़ करें", "Làm nét")
tr("scanner_filter_confirm", "Listo", "Concluir", "पूर्ण", "Xong")
tr("scanner_filter_preview", "Vista previa", "Pré-visualização", "पूर्वावलोकन", "Xem trước")
tr("scanner_rotate_left", "Girar a la izquierda", "Girar à esquerda", "बाएँ घुमाएँ", "Xoay trái")
tr("scanner_rotate_right", "Girar a la derecha", "Girar à direita", "दाएँ घुमाएँ", "Xoay phải")
tr("scanner_review_title", "Revisar", "Revisar", "समीक्षा", "Xem lại")
tr("scanner_review_add_more", "Añadir página", "Adicionar página", "पृष्ठ जोड़ें", "Thêm trang")
tr("scanner_review_edit", "Editar", "Editar", "संपादित करें", "Chỉnh sửa")
tr("scanner_review_delete", "Eliminar", "Excluir", "हटाएँ", "Xóa")
tr("scanner_review_delete_confirm", "¿Seguro que deseas eliminar la página %1$d?", "Tem certeza de que deseja excluir a página %1$d?", "क्या आप पृष्ठ %1$d हटाना चाहते हैं?", "Bạn có chắc muốn xóa trang %1$d?")
tr("scanner_review_page_label", "Página %1$d / %2$d", "Página %1$d / %2$d", "पृष्ठ %1$d / %2$d", "Trang %1$d / %2$d")
tr("scanner_review_page_content_desc", "Página %1$d", "Página %1$d", "पृष्ठ %1$d", "Trang %1$d")
tr("scanner_close", "Cerrar", "Fechar", "बंद करें", "Đóng")
tr("scanner_document", "Documento", "Documento", "दस्तावेज़", "Tài liệu")

# --- Viewer ---
tr("viewer_error_prefix", "Error: %1$s", "Erro: %1$s", "त्रुटि: %1$s", "Lỗi: %1$s")
tr("viewer_file_not_found", "El archivo no existe", "O arquivo não existe", "फ़ाइल मौजूद नहीं है", "Tệp không tồn tại")
tr("viewer_page_content_desc", "Página %1$d", "Página %1$d", "पृष्ठ %1$d", "Trang %1$d")

# --- Main ---
tr("main_tab_home", "Inicio", "Início", "होम", "Trang chủ")
tr("main_tab_settings", "Ajustes", "Configurações", "सेटिंग्स", "Cài đặt")
tr("main_scan_button", "Escanear", "Digitalizar", "स्कैन", "Quét")
tr("main_home_title", "Escáner de documentos", "Scanner de documentos", "दस्तावेज़ स्कैनर", "Máy quét tài liệu")
tr("main_home_subtitle", "Escanea y gestiona tus documentos en un solo lugar.", "Digitalize e gerencie seus documentos em um só lugar.", "एक ही जगह पर दस्तावेज़ स्कैन और प्रबंधित करें।", "Quét và quản lý tài liệu của bạn ở một nơi.")
tr("main_quick_scan_title", "Escaneo rápido", "Digitalização rápida", "त्वरित स्कैन", "Quét nhanh")
tr("main_quick_scan_desc", "Toca Escanear para capturar un documento con detección automática de bordes.", "Toque em Digitalizar para capturar um documento com detecção automática de bordas.", "स्वचालित किनारा पहचान के साथ दस्तावेज़ कैप्चर करने के लिए स्कैन टैप करें।", "Nhấn Quét để chụp tài liệu với nhận diện viền tự động.")
tr("main_recent_scans_title", "Escaneos recientes", "Digitalizações recentes", "हाल के स्कैन", "Bản quét gần đây")
tr("main_recent_scans_empty", "Aún no hay escaneos recientes", "Ainda não há digitalizações recentes", "अभी कोई हालिया स्कैन नहीं", "Chưa có bản quét gần đây")
tr("main_recent_open", "Abrir", "Abrir", "खोलें", "Mở")
tr("main_recent_share", "Compartir", "Compartilhar", "साझा करें", "Chia sẻ")
tr("main_recent_rename", "Renombrar", "Renomear", "नाम बदलें", "Đổi tên")
tr("main_recent_rename_title", "Renombrar documento", "Renomear documento", "दस्तावेज़ का नाम बदलें", "Đổi tên tài liệu")
tr("main_recent_rename_label", "Nombre del archivo", "Nome do arquivo", "फ़ाइल नाम", "Tên tệp")
tr("main_recent_rename_suffix", ".pdf", ".pdf", ".pdf", ".pdf")
tr("main_recent_rename_confirm", "Guardar", "Salvar", "सहेजें", "Lưu")
tr("main_recent_rename_success", "Documento renombrado", "Documento renomeado", "दस्तावेज़ का नाम बदला गया", "Đã đổi tên tài liệu")
tr("main_recent_rename_invalid", "Introduce un nombre de archivo válido.", "Digite um nome de arquivo válido.", "मान्य फ़ाइल नाम दर्ज करें।", "Nhập tên tệp hợp lệ.")
tr("main_recent_rename_exists", "Ya existe un documento con este nombre.", "Já existe um documento com este nome.", "इस नाम का दस्तावेज़ पहले से मौजूद है।", "Đã có tài liệu với tên này.")
tr("main_recent_rename_failed", "No se pudo renombrar el documento. Inténtalo de nuevo.", "Não foi possível renomear o documento. Tente novamente.", "दस्तावेज़ का नाम नहीं बदला जा सका। कृपया पुनः प्रयास करें।", "Không thể đổi tên tài liệu. Vui lòng thử lại.")
tr("main_recent_delete", "Eliminar", "Excluir", "हटाएँ", "Xóa")
tr("main_recent_more_actions", "Más acciones", "Mais ações", "और क्रियाएँ", "Thao tác khác")
tr("main_recent_delete_title", "¿Eliminar documento?", "Excluir documento?", "दस्तावेज़ हटाएँ?", "Xóa tài liệu?")
tr("main_recent_delete_message", "%1$s se eliminará permanentemente de este dispositivo. Esta acción no se puede deshacer.", "%1$s será removido permanentemente deste dispositivo. Esta ação não pode ser desfeita.", "%1$s इस डिवाइस से स्थायी रूप से हटा दिया जाएगा। इसे पूर्ववत नहीं किया जा सकता।", "%1$s sẽ bị xóa vĩnh viễn khỏi thiết bị. Không thể hoàn tác.")
tr("main_recent_scan_meta", "%1$s • %2$d páginas • %3$s", "%1$s • %2$d páginas • %3$s", "%1$s • %2$d पृष्ठ • %3$s", "%1$s • %2$d trang • %3$s")
tr("main_recent_size_zero", "0 B", "0 B", "0 B", "0 B")
tr("main_recent_action_failed", "La acción falló. Inténtalo de nuevo.", "A ação falhou. Tente novamente.", "क्रिया विफल रही। कृपया पुनः प्रयास करें।", "Thao tác thất bại. Vui lòng thử lại.")
tr("main_recent_delete_not_found", "Documento no encontrado.", "Documento não encontrado.", "दस्तावेज़ नहीं मिला।", "Không tìm thấy tài liệu.")
tr("main_recent_delete_partial", "Eliminado de la lista, pero no se pudo borrar el archivo.", "Removido da lista, mas o arquivo não pôde ser excluído.", "सूची से हटाया गया, लेकिन फ़ाइल नहीं मिटाई जा सकी।", "Đã xóa khỏi danh sách nhưng không xóa được tệp.")
tr("main_documents_title", "Mis documentos", "Meus documentos", "मेरे दस्तावेज़", "Tài liệu của tôi")
tr("main_search_hint", "Buscar documentos", "Buscar documentos", "दस्तावेज़ खोजें", "Tìm tài liệu")
tr("main_search_clear", "Borrar búsqueda", "Limpar busca", "खोज साफ़ करें", "Xóa tìm kiếm")
tr("main_search_no_results_title", "No hay documentos coincidentes", "Nenhum documento correspondente", "कोई मेल खाता दस्तावेज़ नहीं", "Không có tài liệu phù hợp")
tr("main_search_no_results_message", "Prueba otro nombre o borra la búsqueda para ver todos los documentos.", "Tente outro nome ou limpe a busca para ver todos os documentos.", "सभी दस्तावेज़ देखने के लिए दूसरा नाम आज़माएँ या खोज साफ़ करें।", "Thử tên khác hoặc xóa tìm kiếm để xem tất cả tài liệu.")
tr("main_empty_docs_title", "Aún no hay documentos", "Ainda não há documentos", "अभी कोई दस्तावेज़ नहीं", "Chưa có tài liệu")
tr("main_empty_docs_message", "No tienes documentos aquí. Toca el botón de escanear para capturar el primero.", "Você ainda não tem documentos aqui. Toque em digitalizar para capturar o primeiro.", "यहाँ कोई दस्तावेज़ नहीं है। पहला दस्तावेज़ कैप्चर करने के लिए स्कैन बटन दबाएँ।", "Chưa có tài liệu. Nhấn nút quét bên dưới để chụp tài liệu đầu tiên.")
tr("main_selection_select", "Seleccionar", "Selecionar", "चुनें", "Chọn")
tr("main_selection_cancel", "Cancelar", "Cancelar", "रद्द करें", "Hủy")
tr("main_selection_count", "%1$d seleccionados", "%1$d selecionados", "%1$d चयनित", "Đã chọn %1$d")
tr("main_selection_banner", "Modo selección · %1$d seleccionados", "Modo de seleção · %1$d selecionados", "चयन मोड · %1$d चयनित", "Chế độ chọn · %1$d đã chọn")
tr("main_selection_select_all", "Seleccionar todo", "Selecionar tudo", "सभी चुनें", "Chọn tất cả")
tr("main_selection_deselect_all", "Deseleccionar todo", "Desmarcar tudo", "सभी हटाएँ", "Bỏ chọn tất cả")
tr("main_selection_share", "Compartir", "Compartilhar", "साझा करें", "Chia sẻ")
tr("main_selection_delete", "Eliminar", "Excluir", "हटाएँ", "Xóa")
tr("main_selection_open", "Abrir", "Abrir", "खोलें", "Mở")
tr("main_selection_delete_title", "¿Eliminar documentos seleccionados?", "Excluir documentos selecionados?", "चयनित दस्तावेज़ हटाएँ?", "Xóa tài liệu đã chọn?")
tr("main_selection_delete_message", "Se eliminarán %1$d documento(s) permanentemente. Esta acción no se puede deshacer.", "%1$d documento(s) será(ão) removido(s) permanentemente. Esta ação não pode ser desfeita.", "%1$d दस्तावेज़ स्थायी रूप से हटाए जाएँगे। इसे पूर्ववत नहीं किया जा सकता।", "%1$d tài liệu sẽ bị xóa vĩnh viễn. Không thể hoàn tác.")
tr("main_selection_delete_confirm", "Eliminar", "Excluir", "हटाएँ", "Xóa")
tr("main_selection_deleted", "%1$d documento(s) eliminados", "%1$d documento(s) excluído(s)", "%1$d दस्तावेज़ हटाए गए", "Đã xóa %1$d tài liệu")
tr("main_selection_delete_partial", "Algunos documentos no se pudieron eliminar por completo.", "Alguns documentos não puderam ser removidos completamente.", "कुछ दस्तावेज़ पूरी तरह हटाए नहीं जा सके।", "Một số tài liệu không thể xóa hoàn toàn.")
tr("main_selection_share_failed", "No se pudieron compartir los documentos. Inténtalo de nuevo.", "Não foi possível compartilhar os documentos. Tente novamente.", "दस्तावेज़ साझा नहीं हो सके। कृपया पुनः प्रयास करें।", "Không thể chia sẻ tài liệu. Vui lòng thử lại.")
tr("main_selection_share_chooser", "Compartir documentos mediante", "Compartilhar documentos via", "दस्तावेज़ साझा करें", "Chia sẻ tài liệu qua")
tr("main_selection_item_cd", "Seleccionar %1$s", "Selecionar %1$s", "%1$s चुनें", "Chọn %1$s")
tr("main_settings_title", "Ajustes", "Configurações", "सेटिंग्स", "Cài đặt")
tr("main_settings_subtitle", "Configura tu experiencia de escaneo.", "Configure sua experiência de digitalização.", "अपना स्कैन अनुभव सेट करें।", "Tùy chỉnh trải nghiệm quét của bạn.")
tr("main_default_quality_title", "Salida predeterminada", "Saída padrão", "डिफ़ॉल्ट आउटपुट", "Đầu ra mặc định")
tr("main_default_quality_desc", "El PDF se optimiza para texto claro y archivos ligeros.", "O PDF é otimizado para texto nítido e arquivos leves.", "PDF स्पष्ट पाठ और हल्की फ़ाइलों के लिए अनुकूलित है।", "PDF được tối ưu cho văn bản rõ và tệp nhẹ.")

# --- Settings ---
tr("settings_section_scanning", "Escaneo", "Digitalização", "स्कैनिंग", "Quét")
tr("settings_section_appearance", "Apariencia", "Aparência", "दिखावट", "Giao diện")
tr("settings_section_storage", "Almacenamiento", "Armazenamento", "संग्रहण", "Bộ nhớ")
tr("settings_section_about", "Acerca de", "Sobre", "जानकारी", "Giới thiệu")
tr("settings_default_capture_mode", "Modo de captura predeterminado", "Modo de captura padrão", "डिफ़ॉल्ट कैप्चर मोड", "Chế độ chụp mặc định")
tr("settings_default_page_layout", "Diseño de página predeterminado", "Layout de página padrão", "डिफ़ॉल्ट पृष्ठ लेआउट", "Bố cục trang mặc định")
tr("settings_default_filter", "Filtro predeterminado", "Filtro padrão", "डिफ़ॉल्ट फ़िल्टर", "Bộ lọc mặc định")
tr("settings_pdf_quality", "Calidad del PDF", "Qualidade do PDF", "PDF गुणवत्ता", "Chất lượng PDF")
tr("settings_theme", "Tema", "Tema", "थीम", "Giao diện")
tr("settings_dynamic_color", "Color dinámico", "Cor dinâmica", "डायनामिक रंग", "Màu động")
tr("settings_dynamic_color_desc", "Usar colores Material You en Android 12+", "Usar cores Material You no Android 12+", "Android 12+ पर Material You रंग उपयोग करें", "Dùng màu Material You trên Android 12+")
tr("settings_storage_usage", "Espacio usado", "Espaço usado", "उपयोग किया गया संग्रहण", "Dung lượng đã dùng")
tr("settings_storage_usage_loading", "Calculando…", "Calculando…", "गणना हो रही है…", "Đang tính toán…")
tr("settings_storage_summary", "%1$d archivos • %2$s", "%1$d arquivos • %2$s", "%1$d फ़ाइलें • %2$s", "%1$d tệp • %2$s")
tr("settings_clear_all_data", "Borrar todos los escaneos", "Limpar todas as digitalizações", "सभी स्कैन साफ़ करें", "Xóa tất cả bản quét")
tr("settings_clear_all_data_desc", "Elimina todos los PDF guardados y el historial de escaneos", "Exclui todos os PDFs salvos e o histórico de digitalizações", "सभी सहेजे PDF और स्कैन इतिहास हटाएँ", "Xóa mọi PDF đã lưu và lịch sử quét")
tr("settings_rate_app", "Valorar la app", "Avaliar o app", "ऐप को रेट करें", "Đánh giá ứng dụng")
tr("settings_rate_app_desc", "Deja tu opinión en Google Play", "Deixe seu feedback no Google Play", "Google Play पर प्रतिक्रिया दें", "Gửi phản hồi trên Google Play")
tr("settings_privacy_policy", "Política de privacidad", "Política de privacidade", "गोपनीयता नीति", "Chính sách quyền riêng tư")
tr("settings_privacy_policy_desc", "Cómo tratamos tus datos", "Como tratamos seus dados", "हम आपके डेटा को कैसे संभालते हैं", "Cách chúng tôi xử lý dữ liệu của bạn")
tr("settings_privacy_policy_error", "No se pudo abrir la política de privacidad. Inténtalo de nuevo.", "Não foi possível abrir a política de privacidade. Tente novamente.", "गोपनीयता नीति नहीं खुल सकी। कृपया पुनः प्रयास करें।", "Không mở được chính sách quyền riêng tư. Vui lòng thử lại.")
tr("settings_about", "Acerca de", "Sobre", "जानकारी", "Giới thiệu")
tr("settings_about_desc", "Información y versión de la app", "Informações e versão do app", "ऐप जानकारी और संस्करण", "Thông tin và phiên bản ứng dụng")
tr("settings_capture_auto", "Captura automática", "Captura automática", "स्वचालित कैप्चर", "Chụp tự động")
tr("settings_capture_manual", "Captura manual", "Captura manual", "मैनुअल कैप्चर", "Chụp thủ công")
tr("settings_page_single", "Una página", "Página única", "एक पृष्ठ", "Một trang")
tr("settings_page_multi", "Varias páginas", "Várias páginas", "कई पृष्ठ", "Nhiều trang")
tr("settings_theme_system", "Predeterminado del sistema", "Padrão do sistema", "सिस्टम डिफ़ॉल्ट", "Theo hệ thống")
tr("settings_theme_light", "Claro", "Claro", "हल्का", "Sáng")
tr("settings_theme_dark", "Oscuro", "Escuro", "गहरा", "Tối")
tr("settings_pdf_standard", "Estándar (archivo más pequeño)", "Padrão (arquivo menor)", "मानक (छोटी फ़ाइल)", "Tiêu chuẩn (tệp nhỏ hơn)")
tr("settings_pdf_high", "Alta calidad", "Alta qualidade", "उच्च गुणवत्ता", "Chất lượng cao")
tr("settings_clear_data_title", "¿Borrar todos los escaneos?", "Limpar todas as digitalizações?", "सभी स्कैन साफ़ करें?", "Xóa tất cả bản quét?")
tr("settings_clear_data_message", "Se eliminarán permanentemente todos los PDF y el historial de escaneos de este dispositivo.", "Todos os PDFs e o histórico serão removidos permanentemente deste dispositivo.", "इस डिवाइस से सभी PDF और स्कैन इतिहास स्थायी रूप से हटा दिए जाएँगे।", "Mọi PDF và lịch sử quét sẽ bị xóa vĩnh viễn khỏi thiết bị.")
tr("settings_clear_data_confirm", "Borrar", "Limpar", "साफ़ करें", "Xóa")
tr("settings_clear_data_cancel", "Cancelar", "Cancelar", "रद्द करें", "Hủy")
tr("settings_clear_data_success", "Todos los escaneos se han borrado.", "Todas as digitalizações foram limpas.", "सभी स्कैन साफ़ कर दिए गए।", "Đã xóa tất cả bản quét.")
tr("settings_clear_data_failed", "No se pudo borrar el almacenamiento. Inténtalo de nuevo.", "Não foi possível limpar o armazenamento. Tente novamente.", "संग्रहण साफ़ नहीं हो सका। कृपया पुनः प्रयास करें।", "Không thể xóa bộ nhớ. Vui lòng thử lại.")
tr("settings_save_failed", "No se pudo guardar el ajuste. Inténtalo de nuevo.", "Não foi possível salvar a configuração. Tente novamente.", "सेटिंग सहेजी नहीं जा सकी। कृपया पुनः प्रयास करें।", "Không lưu được cài đặt. Vui lòng thử lại.")
tr("settings_version_label", "v%1$s", "v%1$s", "v%1$s", "v%1$s")
tr("settings_version_suffix_dev", " (Dev)", " (Dev)", " (Dev)", " (Dev)")
tr("settings_version_suffix_staging", " (Staging)", " (Staging)", " (Staging)", " (Staging)")
tr("settings_environment_production", "Producción", "Produção", "उत्पादन", "Sản xuất")
tr("settings_environment_development", "Desarrollo", "Desenvolvimento", "विकास", "Phát triển")
tr("settings_environment_staging", "Preproducción", "Homologação", "स्टेजिंग", "Staging")
tr("settings_about_message", "Escaneo profesional de documentos con detección de bordes, filtros y exportación a PDF.\n\nVersión: %1$s\nEntorno: %2$s", "Digitalização profissional com detecção de bordas, filtros e exportação em PDF.\n\nVersão: %1$s\nAmbiente: %2$s", "किनारा पहचान, फ़िल्टर और PDF निर्यात के साथ पेशेवर दस्तावेज़ स्कैनिंग।\n\nसंस्करण: %1$s\nवातावरण: %2$s", "Quét tài liệu chuyên nghiệp với nhận diện viền, bộ lọc và xuất PDF.\n\nPhiên bản: %1$s\nMôi trường: %2$s")
tr("settings_about_close", "Cerrar", "Fechar", "बंद करें", "Đóng")
tr("settings_play_store_unavailable", "Google Play no está disponible en este dispositivo.", "O Google Play não está disponível neste dispositivo.", "इस डिवाइस पर Google Play उपलब्ध नहीं है।", "Google Play không khả dụng trên thiết bị này.")

# --- In-app review ---
tr("review_prompt_title", "¿Te gusta %1$s?", "Gostando do %1$s?", "%1$s पसंद आ रहा है?", "Bạn thích %1$s chứ?")
tr(
    "review_prompt_message_no_ads",
    "¿La app te funciona bien? Una valoración rápida nos motiva a mantener LzyScan rápido y 100% sin anuncios.",
    "O app está funcionando bem para você? Uma avaliação rápida nos motiva a manter o LzyScan rápido e 100% sem anúncios!",
    "क्या ऐप आपके लिए सहज चल रहा है? एक त्वरित रेटिंग हमें LzyScan को तेज़ और 100% विज्ञापन-मुक्त रखने की प्रेरणा देती है!",
    "App có chạy mượt không? Đánh giá nhanh giúp chúng mình có động lực giữ LzyScan nhanh và 100% không quảng cáo!",
)
tr(
    "review_prompt_message_with_ads",
    "¿LzyScan te ahorra tiempo? Una valoración rápida en Google Play nos ayuda a seguir mejorando la app. ¡Gracias por tu apoyo!",
    "O LzyScan está economizando seu tempo? Uma avaliação rápida no Google Play nos ajuda a continuar melhorando o app. Obrigado pelo seu apoio!",
    "क्या LzyScan आपका समय बचा रहा है? Google Play पर एक त्वरित रेटिंग ऐप को बेहतर बनाने में मदद करती है। आपके समर्थन के लिए धन्यवाद!",
    "LzyScan có tiết kiệm thời gian cho bạn không? Đánh giá nhanh trên Google Play giúp chúng mình cải thiện app. Cảm ơn bạn đã ủng hộ!",
)
tr("review_prompt_rate", "Valorar ahora", "Avaliar agora", "अभी रेट करें", "Đánh giá ngay")
tr("review_prompt_later", "Quizá más tarde", "Talvez depois", "बाद में", "Để sau nhé")

PLURALS: dict[str, dict[str, dict[str, str]]] = {
    "main_search_results_count": {
        "es": {"one": "%d resultado", "other": "%d resultados"},
        "pt": {"one": "%d resultado", "other": "%d resultados"},
        "hi": {"one": "%d परिणाम", "other": "%d परिणाम"},
        "vi": {"one": "%d kết quả", "other": "%d kết quả"},
    }
}


def escape_xml(text: str) -> str:
    return (
        text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("'", "\\'")
        .replace('"', "&quot;")
    )


def is_translatable_false(elem: ET.Element) -> bool:
    return elem.get("translatable", "true").lower() == "false"


def parse_base() -> tuple[list[tuple[str, str]], list[tuple[str, dict[str, str]]]]:
    root = ET.parse(BASE_XML).getroot()
    strings: list[tuple[str, str]] = []
    plurals: list[tuple[str, dict[str, str]]] = []
    for child in root:
        if child.tag == "string":
            name = child.attrib["name"]
            if name in SKIP_NAMES or is_translatable_false(child):
                continue
            strings.append((name, child.text or ""))
        elif child.tag == "plurals":
            name = child.attrib["name"]
            items = {
                item.attrib["quantity"]: item.text or ""
                for item in child.findall("item")
            }
            plurals.append((name, items))
    return strings, plurals


def write_locale(locale: str, strings: list[tuple[str, str]], plurals: list[tuple[str, dict[str, str]]]) -> None:
    out = ROOT / f"app/src/main/res/values-{locale}/strings.xml"
    out.parent.mkdir(parents=True, exist_ok=True)
    lines = ['<?xml version="1.0" encoding="utf-8"?>', "<resources>"]
    missing: list[str] = []

    for name, en in strings:
        if name in T:
            value = T[name][locale]
        else:
            value = en
            missing.append(name)
        lines.append(f'    <string name="{name}">{escape_xml(value)}</string>')

    for name, en_items in plurals:
        lines.append(f'    <plurals name="{name}">')
        p = PLURALS.get(name, {})
        locale_items = p.get(locale, en_items)
        for qty in ("zero", "one", "two", "few", "many", "other"):
            if qty in locale_items:
                lines.append(
                    f'        <item quantity="{qty}">{escape_xml(locale_items[qty])}</item>'
                )
            elif qty in en_items:
                lines.append(
                    f'        <item quantity="{qty}">{escape_xml(en_items[qty])}</item>'
                )
        lines.append("    </plurals>")

    lines.append("</resources>")
    out.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"Wrote {out} ({len(strings)} strings, {len(plurals)} plurals)")
    if missing:
        print(f"  WARNING: {len(missing)} keys fell back to English: {missing[:5]}...")


def main() -> None:
    strings, plurals = parse_base()
    base_names = {n for n, _ in strings}
    dict_names = set(T.keys())
    extra = dict_names - base_names
    if extra:
        print(f"Unused translation keys: {sorted(extra)}")
    for locale in LOCALES:
        write_locale(locale, strings, plurals)


if __name__ == "__main__":
    main()