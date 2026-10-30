# Reaching the Web-888 from the Internet with frp

The unit connects **out** to an frp server (`frps`) on a VPS; the VPS's web
server (nginx) faces the public, with HTTPS. Nothing is opened on the
unit's router.

```
browser --HTTPS--> nginx (VPS, web888.example.org)
                     --HTTP--> 127.0.0.1:18088 (frps, on the VPS)
                                 <== tunnel, the unit dialled out (VPS:7000) ==
                               frpc (on the unit) --> web888d 127.0.0.1:8073
```

Added in web888-ng-os after v0.2.0 (the `frp` recipe, `frpc.service`, the
`frp_*` settings of `web888-setup`). Tested on a Web-888 (2026-10-30) with
the pieces patched into a v0.2.0 image: `web888-setup` took the settings,
frpc logged in to the VPS's frps and the VPS reached web888d through the
tunnel. The recipe itself has not been built with Yocto yet. The
settings logic is also tested on the host (`frpc verify` accepts the
generated file).

## 1. On the card: `web888.txt`

```
frp_server = vps.example.org:7000
frp_token = <the frps token>
frp_remote_port = 18088
#frp_name = web888
```

* `frp_server`: host and port of `frps` (port default 7000). No
  `frp_server`: no tunnel (frpc does not start).
* `frp_token`: the server's `auth.token`. Once the unit has taken it, it is
  kept on the configuration partition (`/config/web888-setup/frp-token`,
  mode 0600) and the line reads `frp_token = (set)`, as with the admin
  password. Write a new token on the line to change it.
* `frp_remote_port`: the port `frps` opens on the VPS for this unit. It
  must be **unused**: one port per tunnel, one frpc per port (a second
  client for the same port or name is refused by `frps`).
* `frp_name`: the tunnel's name at `frps`, unique among its clients.

`web888-setup.log` says what was applied. Every boot rewrites frpc's
configuration in `/run/web888/frpc.toml` (mode 0600) and starts
`frpc.service` only if it exists. `loginFailExit = false`: frpc keeps
trying while the VPS or the network is away.

With the tunnel on, `web888-setup` also makes sure `trusted_proxies` under
`[web]` in `/config/web888d.toml` has `"127.0.0.1"`: it adds the key, or
adds the address to a one-line list (web888d itself writes
`trusted_proxies = []`). A list over several lines is left alone and
logged as a problem. The connections arrive from frpc on 127.0.0.1; web888d then takes
each visitor's address from the `X-Forwarded-For` that nginx sets. Without
this all visitors would count as one address: `[limits] per_ip` (default
4 sessions) would be shared by the whole Internet.

## 2. On the VPS

Check `frps` (`/etc/frp/frps.toml`) and the firewall:

```toml
bindPort = 7000
auth.method = "token"
auth.token = "..."             # the same as frp_token
proxyBindAddr = "127.0.0.1"    # tunnel ports only for the local nginx
allowPorts = [{ start = 18000, end = 18999 }]   # optional
```

`proxyBindAddr = "127.0.0.1"` keeps port 18088 off the public interface;
the web server is then the only way in. **The default is 0.0.0.0**: the
tunnel port then answers plain HTTP (the login and admin pages included)
to anyone, past your HTTPS. If the web server runs in a container and
reaches the host through the Docker bridge, bind to the bridge's address
instead, or block the port from outside in the firewall. Only port 7000
(frps) and 80/443 need to be open. frpc and frps should be close in version (the unit has frp
**0.71.1**; configurations are TOML, which needs 0.52 or newer).

nginx, as a site of its own (HTTPS with your certificate, e.g. certbot):

```nginx
server {
    listen 443 ssl;
    server_name web888.example.org;
    # ssl_certificate ... (certbot)

    location / {
        proxy_pass http://127.0.0.1:18088;
        proxy_http_version 1.1;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $remote_addr;   # not $proxy_add_x_forwarded_for: no spoofing
        proxy_set_header X-Forwarded-Proto $scheme;
        # WebSocket (/ws): audio and waterfall
        proxy_set_header Upgrade    $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout  1h;
        proxy_send_timeout  1h;
        proxy_buffering off;
    }
}
```

The browser talks HTTPS to nginx, so audio plays with the AudioWorklet
(no plain-HTTP fallback). The unit serves plain HTTP over the tunnel
(frpc connects to port 8073's HTTP side; web888d answers HTTP and HTTPS
on that port).

## 3. What is public

The whole web888d port: the receiver **and the admin pages**
(`#/admin`, `/api/admin/*`). The admin login is rate-limited (5 failed
tries per address per minute) and there is no default password, but a
strong `admin_password` is a must. To keep the admin API off the
Internet, refuse it at nginx and use the LAN address (or SSH) for admin:

```nginx
    location /api/admin { return 403; }
```

(Not tried on a unit: check that the admin pages then work from the LAN
address.) Sessions per visitor address
follow `[limits] per_ip`; a club's own network can go in `[limits]
trusted`.

## 4. Checks

On the VPS: `curl -I http://127.0.0.1:18088/` answers 200 when the tunnel
is up. `journalctl -u frps` on the VPS and, on the unit (SSH),
`journalctl -u frpc` show the login and the proxy. Then open
`https://web888.example.org/`; in the admin pages' sessions list the
visitors' addresses should be theirs, not 127.0.0.1 (otherwise
`trusted_proxies` or nginx's `X-Forwarded-For` is wrong).

If the same name or port is also used by another frpc (an old laptop
tunnel, say), `frps` refuses the second client: stop the first, or give
this one another `frp_name` and `frp_remote_port`.

## Sources (checked 2026-10-30)

* frp releases and checksums: https://github.com/fatedier/frp/releases/tag/v0.71.1
  (`frp_0.71.1_linux_arm.tar.gz`, SHA-256 in `frp_sha256_checksums.txt`;
  the recipe pins it).
* frp configuration (TOML): https://gofrp.org/en/docs/reference/client-configures/
  (`loginFailExit`, `auth.*`, `[[proxies]]`).
