# Data migration: default app settings (same as pref_settings.xml / app defaults)

from django.db import migrations


def defaults():
    return [
        ("pref_speed_enabled", "false"),
        ("pref_confirm_remove", "false"),
        ("pref_start_scan_immediate", "false"),
        ("pref_double_column_display", "false"),
        ("pref_language", "auto"),
        ("pref_ui_mode_night", "0"),
        ("pref_prefer_ipv6", "false"),
        ("pref_local_dns_enabled", "false"),
        ("pref_fake_dns_enabled", "false"),
        ("pref_append_http_proxy", "false"),
        ("pref_vpn_dns", ""),
        ("pref_vpn_bypass_lan", "1"),
        ("pref_vpn_interface_address_config_index", "0"),
        ("pref_vpn_mtu", "1500"),
        ("pref_use_hev_tunnel_v2", "true"),
        ("pref_hev_tunnel_loglevel", "warn"),
        ("pref_hev_tunnel_rw_timeout_v2", "300,60"),
        ("pref_sniffing_enabled", "true"),
        ("pref_route_only_enabled", "false"),
        ("pref_proxy_sharing_enabled", "false"),
        ("pref_allow_insecure", "false"),
        ("pref_socks_port", "10808"),
        ("pref_remote_dns", ""),
        ("pref_domestic_dns", ""),
        ("pref_dns_hosts", ""),
        ("pref_core_loglevel", "warning"),
        ("pref_outbound_domain_resolve_method", "1"),
        ("pref_mux_enabled", "false"),
        ("pref_mux_concurrency", "8"),
        ("pref_mux_xudp_concurrency", "8"),
        ("pref_mux_xudp_quic", "reject"),
        ("pref_fragment_enabled", "false"),
        ("pref_fragment_length", "50-100"),
        ("pref_fragment_interval", "10-20"),
        ("pref_fragment_packets", "tlshello"),
        ("pref_auto_update_subscription", "false"),
        ("pref_auto_update_interval", "1440"),
        ("pref_is_booted", "false"),
        ("pref_auto_remove_invalid_after_test", "false"),
        ("pref_auto_sort_after_test", "false"),
        ("pref_delay_test_url", "https://www.gstatic.com/generate_204"),
        ("pref_ip_api_url", "https://api.ip.sb/geoip"),
        ("pref_mode", "VPN"),
    ]


def forwards(apps, schema_editor):
    AppSetting = apps.get_model("servers", "AppSetting")
    for key, value in defaults():
        AppSetting.objects.get_or_create(key=key, defaults={"value": value})


def backwards(apps, schema_editor):
    AppSetting = apps.get_model("servers", "AppSetting")
    keys = [k for k, _ in defaults()]
    AppSetting.objects.filter(key__in=keys).delete()


class Migration(migrations.Migration):

    dependencies = [
        ("servers", "0002_appsetting"),
    ]

    operations = [
        migrations.RunPython(forwards, backwards),
    ]
