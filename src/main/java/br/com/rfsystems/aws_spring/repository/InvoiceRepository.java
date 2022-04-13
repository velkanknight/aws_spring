package br.com.rfsystems.aws_spring.repository;

import br.com.rfsystems.aws_spring.model.Invoice;
import org.aspectj.weaver.Dump;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends CrudRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findAllByCustomerName(String customerName);

}
