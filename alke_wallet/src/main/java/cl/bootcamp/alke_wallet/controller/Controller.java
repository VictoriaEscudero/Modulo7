package cl.bootcamp.alke_wallet.controller;

import cl.bootcamp.alke_wallet.model.Transaction;
import cl.bootcamp.alke_wallet.model.User;
import cl.bootcamp.alke_wallet.repository.TransactionRepository;
import cl.bootcamp.alke_wallet.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Controller
public class Controller {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpServletRequest request, Model model) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("user", userOpt.get());
            return "redirect:/menu";
        } else {
            return "redirect:/?error=true";
        }
    }

    @GetMapping("/menu")
    public String menuPrincipal(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            model.addAttribute("name", user.getName());
            model.addAttribute("balance", user.getBalance());
            return "menu";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/deposit")
    public String depositMoney() {
        return "deposit";
    }

    @PostMapping("/deposit")
    public String depositMoney(HttpServletRequest request, @RequestParam double amount, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            user.setBalance((int) (user.getBalance() + amount));
            userRepository.save(user);

            Transaction transaction = new Transaction();
            transaction.setUser(user);
            transaction.setDetails("Deposit");
            transaction.setAmount(amount);
            transactionRepository.save(transaction);

            session.setAttribute("user", user);
            model.addAttribute("name", user.getName());
            model.addAttribute("balance", user.getBalance());

            return "redirect:/menu";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/withdraw")
    public String withdraw() {
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdraw(HttpServletRequest request, @RequestParam double amount, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            if (user.getBalance() >= amount) {
                user.setBalance((int) (user.getBalance() - amount));
                userRepository.save(user);

                Transaction transaction = new Transaction();
                transaction.setUser(user);
                transaction.setDetails("Withdraw");
                transaction.setAmount(amount);
                transactionRepository.save(transaction);

                session.setAttribute("user", user);
                model.addAttribute("name", user.getName());
                model.addAttribute("balance", user.getBalance());

                return "redirect:/menu";
            } else {
                model.addAttribute("error", "Saldo insuficiente");
                return "withdraw";
            }
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/sendmoney")
    public String sendmoney(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            List<User> contacts = userRepository.findAll();
            model.addAttribute("contacts", contacts);
            return "sendmoney";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/sendmoney")
    public String sendmoney(HttpServletRequest request, @RequestParam double amount, @RequestParam String contact, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            Optional<User> receiverOpt = userRepository.findByEmail(contact);

            if (receiverOpt.isPresent()) {
                User receiver = receiverOpt.get();

                if (user.getBalance() >= amount) {
                    receiver.setBalance((int) (receiver.getBalance() + amount));
                    userRepository.save(receiver);

                    user.setBalance((int) (user.getBalance() - amount));
                    userRepository.save(user);

                    Transaction transactionOut = new Transaction();
                    transactionOut.setUser(user);
                    transactionOut.setDetails("Se realiza una transferencia a " + receiver.getName());
                    transactionOut.setAmount(-amount);
                    transactionRepository.save(transactionOut);

                    Transaction transactionIn = new Transaction();
                    transactionIn.setUser(receiver);
                    transactionIn.setDetails("Se recibe una transferencia de " + user.getName());
                    transactionIn.setAmount(amount);
                    transactionRepository.save(transactionIn);

                    session.setAttribute("user", user);
                    model.addAttribute("name", user.getName());
                    model.addAttribute("balance", user.getBalance());

                    return "redirect:/menu";

                } else {
                    model.addAttribute("error", "Saldo insuficiente para realizar la operación");
                    return "sendmoney";
                }

            } else {
                model.addAttribute("error", "No existe usuario");
                return "sendmoney";
            }

        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/transactions")
    public String transactions(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user != null) {
            List<Transaction> transactions = transactionRepository.findByUser(user);
            model.addAttribute("transactions", transactions);
            return "transactions";
        } else {
            return "redirect:/";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        session.invalidate();
        return "redirect:/";
    }
}