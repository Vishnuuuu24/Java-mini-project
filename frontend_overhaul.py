import os
import glob
import re

def process_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. ADD MODERN ICONS (LUCIDE) TO HEAD
    if 'lucide' not in content:
        content = content.replace('</head>', '    <script src="https://unpkg.com/lucide@latest"></script>\n</head>')

    # 2. FIX BUTTONS
    content = re.sub(r'<button([^>]*)>', lambda m: '<button' + m.group(1) + '>' if 'class=' in m.group(1) else '<button class="btn"' + m.group(1) + '>', content)
    content = content.replace('class=""', 'class="btn"')
    content = re.sub(r'class="btn btn-primary"', 'class="btn"', content)
    content = re.sub(r'class="btn btn-secondary"', 'class="btn"', content)
    content = re.sub(r'class="btn btn-danger"', 'class="btn btn-danger"', content)

    # 3. FIX TABLES
    content = re.sub(r'<table([^>]*)>', lambda m: '<div class="table-responsive"><table class="table"' + m.group(1).replace('class="', 'class="table ') + '>' if 'class=' in m.group(1) else '<div class="table-responsive"><table class="table"' + m.group(1) + '>', content)
    content = content.replace('</table>', '</table></div>')
    
    # Remove duplicate classes in table if any
    content = content.replace('class="table table"', 'class="table"')

    # 4. FIX FORMS & INPUTS (Basic)
    content = re.sub(r'<input([^>]*)>', lambda m: '<input' + m.group(1) + '>' if 'type="hidden"' in m.group(1) or 'type="radio"' in m.group(1) or 'type="checkbox"' in m.group(1) or 'class=' in m.group(1) else '<input class="form-control"' + m.group(1) + '>', content)
    content = re.sub(r'<select([^>]*)>', lambda m: '<select' + m.group(1) + '>' if 'class=' in m.group(1) else '<select class="form-control"' + m.group(1) + '>', content)
    content = re.sub(r'<textarea([^>]*)>', lambda m: '<textarea' + m.group(1) + '>' if 'class=' in m.group(1) else '<textarea class="form-control"' + m.group(1) + '>', content)
    
    # 5. WRAP MAIN CONTENT IN CONTAINER (If not already wrapped)
    # Detect the nav closing tag
    if '</nav>' in content and '<div class="container">' not in content and '<div class="container mt-4">' not in content:
        parts = content.split('</nav>', 1)
        if len(parts) == 2:
            body_close = parts[1].rfind('</body>')
            if body_close != -1:
                inner = parts[1][:body_close]
                # Try to wrap it
                parts[1] = '\n<div class="container">\n' + inner + '\n</div>\n' + parts[1][body_close:]
                content = parts[0] + '</nav>' + parts[1]

    # 6. ENHANCE LOGIN HTML SPECIFICALLY
    if 'login.html' in filepath:
        content = content.replace('<body>', '<body>\n<div class="login-container">\n  <div class="card">\n    <h3 class="card-title text-center">Welcome Back</h3>\n    <p class="text-center text-muted mb-4">Please log in to continue</p>')
        content = content.replace('</body>', '  </div>\n</div>\n</body>')
        content = content.replace('<h2>LOGIN</h2>', '')
        content = content.replace('<div class="error-message"', '<div class="alert alert-danger"')
        content = content.replace('<div class="success-message"', '<div class="alert alert-success"')

    # 7. INITIALIZE LUCIDE ICONS
    if 'lucide.createIcons()' not in content:
        content = content.replace('</body>', '    <script>\n        lucide.createIcons();\n    </script>\n</body>')

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

for filepath in glob.glob('src/main/resources/templates/**/*.html', recursive=True):
    process_file(filepath)

print("Frontend HTML layout upgraded across all files!")
