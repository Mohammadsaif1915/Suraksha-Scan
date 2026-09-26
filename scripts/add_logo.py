import os
import glob

frontend_dir = r"c:\Users\Administrator\Desktop\SurakshaScan\frontend"
html_files = glob.glob(os.path.join(frontend_dir, "*.html"))

logo_img = '<img src="img/logo.png" alt="Logo" style="height:1.2em; margin-right:8px; vertical-align:middle;">'

replacements = [
    (
        '<a href="index.html">Suraksha<em>Scan</em></a>',
        f'<a href="index.html" style="display:flex; align-items:center; text-decoration:none;">{logo_img}Suraksha<em>Scan</em></a>'
    ),
    (
        '<a href="#top" class="brand">Suraksha<em>Scan</em></a>',
        f'<a href="#top" class="brand" style="display:flex; align-items:center; text-decoration:none;">{logo_img}Suraksha<em>Scan</em></a>'
    ),
    (
        '<div class="brand">Suraksha<em>Scan</em></div>',
        f'<div class="brand" style="display:flex; align-items:center;">{logo_img}Suraksha<em>Scan</em></div>'
    ),
    (
        '<h1>SurakshaScan</h1>',
        f'<h1 style="display:flex; align-items:center;">{logo_img}SurakshaScan</h1>'
    )
]

for filepath in html_files:
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()
    
    modified = False
    for old_str, new_str in replacements:
        if old_str in content:
            content = content.replace(old_str, new_str)
            modified = True
            
    if modified:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {os.path.basename(filepath)}")

print("Done updating HTML files.")
